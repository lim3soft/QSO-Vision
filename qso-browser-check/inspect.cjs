const { chromium } = require('playwright');
const fs = require('fs');
(async () => {
  const browser = await chromium.launch({executablePath: '/root/.cache/ms-playwright/chromium-1248/chrome-linux64/chrome', headless: true, args: ['--no-sandbox']});
  const page = await browser.newPage({viewport: {width: 1440, height: 1000}});
  page.on('pageerror', error => console.log('PAGE ERROR', error.message));
  await page.goto(process.env.QSO_URL || 'https://brandmeister.network/', {waitUntil: 'domcontentloaded', timeout: 60000});
  await page.waitForTimeout(12000);
  console.log('URL',page.url());
  console.log((await page.locator('body').innerText()).slice(0,16000));
  console.log('LINKS', JSON.stringify(await page.locator('a').evaluateAll(els => els.map(e=>({text:e.innerText,href:e.getAttribute('href')})))));
  console.log('INPUTS',JSON.stringify(await page.locator('input,select,button').evaluateAll(els=>els.map(e=>({tag:e.tagName,text:e.innerText,id:e.id,placeholder:e.getAttribute('placeholder'),type:e.type})))));
  fs.writeFileSync('/tmp/qso-rendered.html', await page.content());
  await page.screenshot({path:'/tmp/qso-rendered.png',fullPage:true});
  await browser.close();
})().catch(e=>{console.error(e);process.exit(1)});
