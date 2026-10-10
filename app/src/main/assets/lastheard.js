// Read the official rendered Last Heard UI. No private API or synthetic events.
(function (wanted) {
  const result = {tg: wanted, status: 'Loading BrandMeister', rows: []};
  try {
    const root = document.querySelector('#query-builder');
    if (!root) return result;
    let rule = root.querySelector('.rules-group-body .rules-list .rule-container');
    if (!rule) {
      const add = Array.from(root.querySelectorAll('button')).find(b => b.textContent.trim() === 'Rule');
      if (add) add.click();
      result.status = 'Preparing talkgroup filter';
      return result;
    }
    const field = rule.querySelector('select[aria-label="Filter field"]');
    const operator = rule.querySelector('select[aria-label="Filter operator"]');
    const value = rule.querySelector('input[aria-label="Filter value"]');
    const set = (el, v) => {
      el.value = v;
      el.dispatchEvent(new Event('input', {bubbles: true}));
      el.dispatchEvent(new Event('change', {bubbles: true}));
    };
    if (!field || !operator || !value) { result.status = 'Filter controls unavailable'; return result; }
    // Separate renders are necessary: changing field replaces the value control.
    if (field.value !== 'DestinationID') { set(field, 'DestinationID'); return result; }
    if (operator.value !== 'equal') { set(operator, 'equal'); return result; }
    if (value.value !== String(wanted)) { set(value, String(wanted)); return result; }
    if (window.__qsoFilteredTg !== wanted) {
      const search = document.querySelector('#query-builder-search');
      if (!search) { result.status = 'Search control unavailable'; return result; }
      search.click();
      window.__qsoFilteredTg = wanted;
      result.status = 'Applying TG ' + wanted;
      return result;
    }
    const table = document.querySelector('#lhTable');
    if (!table) { result.status = 'Waiting for Last Heard table'; return result; }
    const headers = Array.from(table.querySelectorAll('thead th')).map(e => e.textContent.trim().toLowerCase());
    const index = name => headers.findIndex(h => h === name);
    const callIndex = index('my call'), destIndex = index('destination');
    if (callIndex < 0 || destIndex < 0) { result.status = 'Last Heard columns changed'; return result; }
    Array.prototype.forEach.call(table.querySelectorAll('tbody tr'), row => {
      const cells = row.querySelectorAll('td');
      if (cells.length !== headers.length) return;
      const dest = cells[destIndex].textContent;
      // matchAll requires Chromium 73; many Nougat devices still ship older WebViews.
      const matches = [], expression = /\((\d+)\)/g;
      let match;
      while ((match = expression.exec(dest)) !== null) matches.push(match);
      if (!matches.length || Number(matches[matches.length - 1][1]) !== wanted) return;
      const callCell = cells[callIndex];
      const call = callCell.textContent.trim().split(/\s|\[/)[0];
      if (!call) return;
      const flag = callCell.querySelector('[title], [aria-label]');
      const country = flag ? (flag.getAttribute('title') || flag.getAttribute('aria-label') || '') : '';
      const text = name => { const i = index(name); return i >= 0 ? cells[i].textContent.trim() : ''; };
      result.rows.push({call, country, tg: wanted, age: text('time'), duration: text('duration'), active: row.classList.contains('lh-row-active')});
    });
    result.status = 'BrandMeister • TG ' + wanted + ' • ' + result.rows.length + ' records';
  } catch (e) { result.status = 'Metadata error: ' + e.message; }
  return result;
})
