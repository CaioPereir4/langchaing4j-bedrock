const BACKEND_BASE_URL = "http://localhost:8080";
const CHAT_ENDPOINT = `${BACKEND_BASE_URL}/chat`;
const SESSION_STORAGE_KEY = "langchain4j.chat.sessionId";

const form = document.querySelector("#chat-form");
const input = document.querySelector("#message-input");
const messages = document.querySelector("#messages");
const statusMessage = document.querySelector("#status");
const sendButton = document.querySelector("#send-button");

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
  messages.scrollTop = messages.scrollHeight;

  return { message, bubble };
}

function setLoading(isLoading) {
  sendButton.disabled = isLoading;
  input.disabled = isLoading;
}

function setStatus(text) {
  statusMessage.textContent = text;
}

form.addEventListener("submit", async (event) => {
  event.preventDefault();

  const text = input.value.trim();

  if (!text) {
    setStatus("Digite uma mensagem antes de enviar.");
    return;
  }

  appendMessage("user", text);
  input.value = "";
  setLoading(true);
  setStatus("Enviando...");

  const assistantMessage = appendMessage("assistant", "Aguardando resposta...", {
    pending: true
  });

  try {
    const response = await fetch(CHAT_ENDPOINT, {
      method: "POST",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify({
        text,
        sessionId: getSessionId()
      })
    });

    if (!response.ok) {
      throw new Error(`Request falhou com status ${response.status}.`);
    }

    const data = await response.json();
    assistantMessage.message.classList.remove("pending");
    assistantMessage.bubble.textContent = data.message || "";
    setStatus("");
  } catch (error) {
    assistantMessage.message.remove();
    appendMessage("error", formatError(error));
    setStatus("Não foi possível concluir a request.");
  } finally {
    setLoading(false);
    input.focus();
  }
});

function formatError(error) {
  if (error instanceof TypeError) {
    return "Falha ao conectar com o backend. Verifique se ele está rodando em http://localhost:8080 e se o navegador permitiu a chamada.";
  }

  return error.message || "Erro inesperado ao chamar o backend.";
}
