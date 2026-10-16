// Read the official rendered Last Heard UI. No private API or synthetic events.
(function (wanted) {
  const result = {tg: wanted, status: 'Loading BrandMeister', rows: []};
  try {
    const root = document.querySelector('#query-builder');
    if (!root) return result;
    // BrandMeister changed the query-builder wrapper from
    // `.rules-group-body` to `.rules-group-container`. Support both DOMs so
    // older and current WebView page versions can prepare the filter.
    let rule = root.querySelector('.rules-list .rule-container');
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
    // Prefer the known table, then discover a likely Last Heard table by its
    // column names. BrandMeister has changed IDs and wrappers in the past.
    const table = document.querySelector('#lhTable') || Array.from(document.querySelectorAll('table')).find(t => {
      const text = Array.from(t.querySelectorAll('thead th')).map(e => e.textContent.trim().toLowerCase());
      return text.some(h => /my call|source/.test(h)) && text.some(h => /destination|talkgroup/.test(h));
    });
    if (!table) { result.status = 'Waiting for Last Heard table'; return result; }
    const headers = Array.from(table.querySelectorAll('thead th')).map(e => e.textContent.trim().toLowerCase());
    const findHeader = names => headers.findIndex(h => names.some(name => h === name || h.indexOf(name) >= 0));
    const index = name => headers.findIndex(h => h === name);
    const callIndex = findHeader(['my call', 'source call', 'source', 'callsign', 'call sign']);
    const destIndex = findHeader(['destination', 'talkgroup', 'destination id']);
    if (callIndex < 0 || destIndex < 0) { result.status = 'Last Heard columns changed'; return result; }
    const timeIndex = findHeader(['time', 'age', 'last heard']);
    const durationIndex = findHeader(['duration', 'length']);
    const secondsSinceHeard = value => {
      const text = String(value || '').toLowerCase().trim();
      if (!text || /now|just/.test(text)) return 0;
      const match = text.match(/(\d+)\s*(s|sec|second|m|min|minute|h|hour)/);
      if (!match) return Number.MAX_SAFE_INTEGER;
      const amount = Number(match[1]);
      if (/^s|sec|second/.test(match[2])) return amount;
      if (/^m|min|minute/.test(match[2])) return amount * 60;
      return amount * 3600;
    };
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
      const text = (i, fallback) => i >= 0 ? cells[i].textContent.trim() : fallback;
      const age = text(timeIndex, '');
      // Do not infer live state from row text: stale rows can retain labels
      // after PTT ends. Only the page's active-row marker plus a recent
      // Last Heard timestamp qualifies as CURRENTLY ON AIR.
      const active = row.classList.contains('lh-row-active') && secondsSinceHeard(age) <= 30;
      result.rows.push({call, country, tg: wanted, age, timestamp: Date.now() - Math.min(secondsSinceHeard(age), 86400) * 1000, duration: text(durationIndex, ''), active});
    });
    result.status = 'BrandMeister • TG ' + wanted + ' • ' + result.rows.length + ' records';
  } catch (e) { result.status = 'Metadata error: ' + e.message; }
  return result;
})
