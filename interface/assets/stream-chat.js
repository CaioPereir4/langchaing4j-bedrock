const BACKEND_BASE_URL = "http://localhost:8080";
const STREAM_ENDPOINT = `${BACKEND_BASE_URL}/chat-with-response-stream`;
const SESSION_STORAGE_KEY = "langchain4j.streamChat.sessionId";

const form = document.querySelector("#chat-form");
const input = document.querySelector("#message-input");
const messages = document.querySelector("#messages");
const statusMessage = document.querySelector("#status");
const sendButton = document.querySelector("#send-button");
const abortButton = document.querySelector("#abort-button");

let currentController = null;

function getSessionId() {
  const existingSessionId = localStorage.getItem(SESSION_STORAGE_KEY);

  if (existingSessionId) {
    return existingSessionId;
  }

  const sessionId = crypto.randomUUID ? crypto.randomUUID() : createFallbackUuid();
  localStorage.setItem(SESSION_STORAGE_KEY, sessionId);
  return sessionId;
}

function createFallbackUuid() {
  return "10000000-1000-4000-8000-100000000000".replace(/[018]/g, (character) => {
    const randomValue = crypto.getRandomValues(new Uint8Array(1))[0];
    return (Number(character) ^ (randomValue & (15 >> (Number(character) / 4)))).toString(16);
  });
}

function appendMessage(role, text, options = {}) {
  const message = document.createElement("article");
  message.className = `message ${role}${options.pending ? " pending" : ""}`;

  const bubble = document.createElement("div");
  bubble.className = "bubble";
  bubble.textContent = text;

  message.appendChild(bubble);
  messages.appendChild(message);

  return { message, bubble };
}

function setStreaming(isStreaming) {
  sendButton.disabled = isStreaming;
  input.disabled = isStreaming;
  abortButton.disabled = !isStreaming;
}

function setStatus(text) {
  statusMessage.textContent = text;
}

abortButton.addEventListener("click", () => {
  if (currentController) {
    currentController.abort();
  }
});

form.addEventListener("submit", async (event) => {
  event.preventDefault();

  const text = input.value.trim();

  if (!text) {
    setStatus("Digite uma mensagem antes de enviar.");
    return;
  }

  currentController = new AbortController();
  appendMessage("user", text);
  input.value = "";
  setStreaming(true);
  setStatus("Recebendo resposta...");

  const assistantMessage = appendMessage("assistant", "", {
    pending: true
  });

  try {
    const response = await fetch(STREAM_ENDPOINT, {
      method: "POST",
      headers: {
        "Accept": "text/event-stream",
        "Content-Type": "application/json"
      },
      body: JSON.stringify({
        text,
        sessionId: getSessionId()
      }),
      signal: currentController.signal
    });

    if (!response.ok) {
      throw new Error(`Request falhou com status ${response.status}.`);
    }

    if (!response.body) {
      throw new Error("O navegador não disponibilizou o stream da resposta.");
    }

    await readServerSentEvents(response.body, (eventName, data) => {
      if (eventName === "token") {
        assistantMessage.bubble.textContent += data;
      }

      if (eventName === "complete") {
        assistantMessage.message.classList.remove("pending");
      }

    });

    assistantMessage.message.classList.remove("pending");
    setStatus("");
  } catch (error) {
    assistantMessage.message.classList.remove("pending");

    if (error.name === "AbortError") {
      if (!assistantMessage.bubble.textContent) {
        assistantMessage.message.remove();
      }

      appendMessage("error", "Request abortada.");
      setStatus("Request abortada.");
      return;
    }

    if (!assistantMessage.bubble.textContent) {
      assistantMessage.message.remove();
    }

    appendMessage("error", formatError(error));
    setStatus("Não foi possível concluir a request.");
  } finally {
    currentController = null;
    setStreaming(false);
    input.focus();
  }
});

async function readServerSentEvents(body, onEvent) {
  const reader = body.getReader();
  const decoder = new TextDecoder("utf-8");
  let buffer = "";

  while (true) {
    const { done, value } = await reader.read();

    if (done) {
      break;
    }

    buffer += decoder.decode(value, { stream: true });
    buffer = buffer.replace(/\r\n/g, "\n");

    let eventBoundary = buffer.indexOf("\n\n");

    while (eventBoundary >= 0) {
      const rawEvent = buffer.slice(0, eventBoundary);
      buffer = buffer.slice(eventBoundary + 2);
      dispatchServerSentEvent(rawEvent, onEvent);
      eventBoundary = buffer.indexOf("\n\n");
    }
  }

  buffer += decoder.decode();

  if (buffer.trim()) {
    dispatchServerSentEvent(buffer, onEvent);
  }
}

function dispatchServerSentEvent(rawEvent, onEvent) {
  const lines = rawEvent.split("\n");
  let eventName = "message";
  const dataLines = [];

  for (const line of lines) {
    if (line.startsWith("event:")) {
      eventName = line.slice(6).trim();
    }

    if (line.startsWith("data:")) {
      dataLines.push(line.substring("data:".length));
    }
  }

  onEvent(eventName, dataLines.join("\n"));
}

function formatError(error) {
  if (error instanceof TypeError) {
    return "Falha ao conectar com o backend. Verifique se ele está rodando em http://localhost:8080 e se o navegador permitiu a chamada.";
  }

  return error.message || "Erro inesperado ao chamar o backend.";
}
