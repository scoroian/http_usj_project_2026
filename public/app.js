// Shared client-side utilities: API calls and UI helpers.
// All pages import this file.

const API_BASE = '';  // same origin

// --- API functions ---

async function apiFetch(method, path, body) {
  const headers = { 'Content-Type': 'application/json' };

  // Si el usuario ha guardado una API key, la añadimos al header
  const apiKey = localStorage.getItem('apiKey');
  if (apiKey) headers['X-API-Key'] = apiKey;

  const options = { method, headers };
  if (body !== undefined) {
    options.body = JSON.stringify(body);
  }
  const res = await fetch(API_BASE + path, options);
  // 204 No Content has no body
  const data = res.status !== 204 ? await res.json().catch(() => null) : null;
  return { ok: res.ok, status: res.status, data };
}

// Permite al usuario configurar la API key desde la UI.
// Se llama desde el botón "API key" de la nav.
function promptApiKey() {
  const current = localStorage.getItem('apiKey') || '';
  const value = prompt('API key (vacío para desactivar):', current);
  if (value === null) return; // cancelado
  if (value === '') {
    localStorage.removeItem('apiKey');
    toast('API key eliminada', 'success');
  } else {
    localStorage.setItem('apiKey', value);
    toast('API key guardada', 'success');
  }
}

const api = {
  getCats:       ()         => apiFetch('GET',    '/cats'),
  getCat:        (id)       => apiFetch('GET',    `/cats/${id}`),
  createCat:     (payload)  => apiFetch('POST',   '/cats', payload),
  updateCat:     (id, data) => apiFetch('PUT',    `/cats/${id}`, data),
  deleteCat:     (id)       => apiFetch('DELETE', `/cats/${id}`),
};

// --- Toast notifications ---

let toastTimeout;

function toast(message, type = 'default') {
  let el = document.getElementById('toast');
  if (!el) {
    el = document.createElement('div');
    el.id = 'toast';
    el.className = 'toast';
    document.body.appendChild(el);
  }

  el.textContent = message;
  el.className = `toast ${type} show`;

  clearTimeout(toastTimeout);
  toastTimeout = setTimeout(() => {
    el.classList.remove('show');
  }, 3000);
}

// --- Modal helpers ---

function openModal(id) {
  document.getElementById(id).classList.add('open');
}

function closeModal(id) {
  document.getElementById(id).classList.remove('open');
}

// Close modal when clicking outside
document.addEventListener('click', (e) => {
  if (e.target.classList.contains('modal-overlay')) {
    e.target.classList.remove('open');
  }
});

// --- Cat card renderer ---

function renderCatCard(cat, { onEdit, onDelete } = {}) {
  const card = document.createElement('div');
  card.className = 'cat-card';
  card.dataset.id = cat.id;
  card.innerHTML = `
    <div class="cat-id">#${cat.id}</div>
    <div class="cat-name">${escapeHtml(cat.name)}</div>
    <div class="cat-meta">${escapeHtml(cat.breed)} &mdash; ${cat.age} ${cat.age === 1 ? 'year' : 'years'}</div>
    <div class="actions">
      <button class="btn btn-secondary btn-sm js-edit">Edit</button>
      <button class="btn btn-danger btn-sm js-delete">Delete</button>
    </div>
  `;

  if (onEdit)   card.querySelector('.js-edit').addEventListener('click', () => onEdit(cat));
  if (onDelete) card.querySelector('.js-delete').addEventListener('click', () => onDelete(cat));

  return card;
}

// Avoid XSS when inserting user data into the DOM
function escapeHtml(str) {
  return String(str)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;');
}

// --- Form helpers ---

function getFormValues(formId) {
  const form = document.getElementById(formId);
  const data = {};
  for (const el of form.querySelectorAll('input, select, textarea')) {
    if (el.name) data[el.name] = el.type === 'number' ? Number(el.value) : el.value;
  }
  return data;
}

function resetForm(formId) {
  document.getElementById(formId).reset();
}

function setFormValues(formId, data) {
  const form = document.getElementById(formId);
  for (const [key, value] of Object.entries(data)) {
    const el = form.querySelector(`[name="${key}"]`);
    if (el) el.value = value;
  }
}
