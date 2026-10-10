const {chromium} = require('playwright');
const fs = require('fs');
(async()=>{
 const browser=await chromium.launch({executablePath:'/usr/bin/chromium',headless:true,args:['--no-sandbox']});
 const page=await browser.newPage({viewport:{width:1440,height:1000}});
 await page.goto('https://brandmeister.network/#/lh',{waitUntil:'domcontentloaded',timeout:60000});
 await page.waitForSelector('#query-builder', {state:'attached',timeout:60000});
 await page.locator('#donation-dismiss').click().catch(()=>{});
 await page.locator('button[data-bs-target="#lastheard-query-builder-panel"]').first().click();
 await page.getByRole('button',{name:'Rule',exact:true}).click();
 console.log('RULE',await page.locator('#query-builder').innerHTML());
 console.log('SELECTS',await page.locator('select').evaluateAll(els=>els.map(e=>e.outerHTML)));
 fs.writeFileSync('/tmp/qso-filter.html',await page.content());
 await browser.close();
})().catch(e=>{console.error(e);process.exit(1)});
