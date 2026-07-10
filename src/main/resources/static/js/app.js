

/* ── Theme toggle ── */
function initThemeToggle() {
  const btn = document.getElementById('themeToggle');
  if (!btn) return;
  btn.addEventListener('click', function() {
    document.documentElement.classList.toggle('dark');
    localStorage.setItem('theme', document.documentElement.classList.contains('dark') ? 'dark' : 'light');
  });
}

/* ── Autocomplete ── */
function initAutocomplete() {
  const inputs = document.querySelectorAll('.search-bar input[type="text"]');
  inputs.forEach(function(input) {
    var container = document.createElement('div');
    container.className = 'autocomplete-wrap';
    input.parentNode.insertBefore(container, input);
    container.appendChild(input);

    var dropdown = document.createElement('div');
    dropdown.className = 'autocomplete-dropdown';
    container.appendChild(dropdown);

    var timer = null;
    function setOpen(open) {
      dropdown.classList.toggle('open', open);
      container.classList.toggle('autocomplete-open', open);
    }
    input.addEventListener('input', function() {
      clearTimeout(timer);
      var val = input.value.trim();
      if (val.length < 2) { setOpen(false); return; }
      timer = setTimeout(function() { fetchSuggestions(val, dropdown, input); }, 250);
    });

    document.addEventListener('click', function(e) {
      if (!container.contains(e.target)) setOpen(false);
    });

    input.addEventListener('keydown', function(e) {
      var items = dropdown.querySelectorAll('.autocomplete-item');
      var active = dropdown.querySelector('.autocomplete-item.active');
      var idx = Array.from(items).indexOf(active);
      if (e.key === 'ArrowDown') {
        e.preventDefault();
        idx = Math.min(idx + 1, items.length - 1);
      } else if (e.key === 'ArrowUp') {
        e.preventDefault();
        idx = Math.max(idx - 1, 0);
      } else if (e.key === 'Enter' && active) {
        e.preventDefault();
        input.value = active.textContent;
        setOpen(false);
        input.form.submit();
        return;
      }
      items.forEach(function(el, i) { el.classList.toggle('active', i === idx); });
      if (items[idx]) items[idx].scrollIntoView({ block: 'nearest' });
    });
  });
}

function fetchSuggestions(query, dropdown, input) {
  var container = dropdown.parentNode;
  var xhr = new XMLHttpRequest();
  xhr.open('GET', '/api/products/suggestions?q=' + encodeURIComponent(query) + '&limit=6');
  xhr.onload = function() {
    if (xhr.status === 200) {
      var data = JSON.parse(xhr.responseText);
      if (data.length === 0) { setOpen(false); return; }
      dropdown.innerHTML = data.map(function(name) {
        var highlighted = name.replace(new RegExp('(' + escapeRegex(query) + ')', 'gi'), '<strong>$1</strong>');
        return '<div class="autocomplete-item" data-value="' + escapeAttr(name) + '">' + highlighted + '</div>';
      }).join('');
      setOpen(true);
      dropdown.querySelectorAll('.autocomplete-item').forEach(function(el) {
        el.addEventListener('click', function() {
          input.value = el.getAttribute('data-value');
          setOpen(false);
          input.form.submit();
        });
      });
    }
  };
  xhr.send();
}

/* ── Infinite scroll ── */
function initInfiniteScroll() {
  var sentinel = document.getElementById('scrollSentinel');
  var grid = document.getElementById('productGrid');
  var pagination = document.getElementById('pagination');
  if (!sentinel || !grid) return;
  if (pagination) pagination.style.display = 'none';
  var currentPage = parseInt(grid.getAttribute('data-page') || '0');
  var totalPages = parseInt(grid.getAttribute('data-total') || '1');
  var loading = false;
  var observer = new IntersectionObserver(function(entries) {
    if (entries[0].isIntersecting && !loading && currentPage + 1 < totalPages) {
      loading = true;
      var nextPage = currentPage + 1;
      var url = new URL(window.location);
      url.pathname = '/api/products/page';
      url.searchParams.set('page', nextPage);
      url.searchParams.set('size', '12');
      fetch(url).then(function(r) { return r.json(); }).then(function(data) {
        data.content.forEach(function(p) {
          var card = document.createElement('div');
          card.className = 'product-card content-fade';
          card.innerHTML =
            (p.imageUrl ? '<div class="product-image"><img src="' + p.imageUrl + '" alt="' + p.name + '" loading="lazy"/></div>' : '') +
            '<h3>' + p.name + '</h3>' +
            (p.brand ? '<div class="brand">' + p.brand + '</div>' : '') +
            '<div class="price">' + p.price + ' ₽</div>' +
            '<span class="stock' + (p.stockQuantity > 0 ? '' : ' out-of-stock') + '">' + (p.stockQuantity > 0 ? 'В наличии' : 'Нет в наличии') + '</span>' +
            '<div class="card-actions">' +
              '<a href="/product/' + p.id + '" class="btn">Подробнее</a>' +
            '</div>';
          grid.appendChild(card);
        });
        currentPage = nextPage;
        grid.setAttribute('data-page', currentPage);
        loading = false;
        if (currentPage + 1 >= totalPages) { sentinel.style.display = 'none'; }
      }).catch(function() { loading = false; });
    }
  }, { rootMargin: '200px' });
  observer.observe(sentinel);
}

/* ── Quick view ── */
window.quickView = function(btn) {
  var id = btn.getAttribute('data-id');
  var modal = document.getElementById('quickViewModal');
  var body = document.getElementById('quickViewBody');
  body.innerHTML = '<div style="text-align:center;padding:2rem;">Загрузка...</div>';
  modal.classList.add('open');
  var xhr = new XMLHttpRequest();
  xhr.open('GET', '/api/products/' + id, true);
  xhr.onload = function() {
    if (xhr.status === 200) {
      var p = JSON.parse(xhr.responseText);
      body.innerHTML =
        '<div class="quick-view-layout">' +
          '<div>' +
            (p.imageUrl ? '<img src="' + p.imageUrl + '" alt="' + p.name + '"/>' : '<div style="background:var(--bg);height:200px;border-radius:8px;display:flex;align-items:center;justify-content:center;">Нет фото</div>') +
          '</div>' +
          '<div>' +
            '<h2>' + p.name + '</h2>' +
            (p.brand ? '<div class="brand">' + p.brand + '</div>' : '') +
            '<div class="price" style="font-size:1.4rem;margin:0.5rem 0;">' + p.price + ' ₽</div>' +
            (p.description ? '<p style="color:var(--text-secondary);font-size:0.9rem;">' + p.description + '</p>' : '') +
            '<div class="meta">' + (p.stockQuantity > 0 ? '<span style="color:var(--success);">В наличии (' + p.stockQuantity + ')</span>' : '<span style="color:var(--danger);">Нет в наличии</span>') + '</div>' +
            '<form method="post" action="/cart/add" style="margin-top:1rem;">' +
              '<input type="hidden" name="productId" value="' + p.id + '"/>' +
              '<input type="number" name="quantity" value="1" min="1" class="qty-input" style="width:60px;"/>' +
              '<button type="submit" class="btn" style="margin-left:0.5rem;">В корзину</button>' +
            '</form>' +
          '</div>' +
        '</div>';
    } else {
      body.innerHTML = '<p style="text-align:center;padding:2rem;color:var(--danger);">Ошибка загрузки</p>';
    }
  };
  xhr.onerror = function() {
    body.innerHTML = '<p style="text-align:center;padding:2rem;color:var(--danger);">Ошибка загрузки</p>';
  };
  xhr.send();
};
window.closeQuickView = function() {
  document.getElementById('quickViewModal').classList.remove('open');
};

/* ── Toast notifications ── */
window.showToast = function(message, type) {
  type = type || 'info';
  var container = document.getElementById('toastContainer');
  if (!container) return;
  var toast = document.createElement('div');
  toast.className = 'toast ' + type;
  toast.textContent = message;
  container.appendChild(toast);
  setTimeout(function() { toast.remove(); }, 4000);
};

document.addEventListener('DOMContentLoaded', function() {
  var params = new URLSearchParams(window.location.search);
  var toastMsg = params.get('toast');
  if (toastMsg) {
    var toastType = params.get('toastType') || 'success';
    showToast(decodeURIComponent(toastMsg), toastType);
    var url = new URL(window.location);
    url.searchParams.delete('toast');
    url.searchParams.delete('toastType');
    window.history.replaceState({}, '', url);
  }
});

/* Init */
document.addEventListener('DOMContentLoaded', function() {
  initAutocomplete();
  initThemeToggle();
  initInfiniteScroll();
});

function escapeRegex(str) {
  return str.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
}
function escapeAttr(str) {
  return str.replace(/"/g, '&quot;').replace(/'/g, '&#39;');
}
