const {chromium}=require('playwright');
const fs=require('fs');
(async()=>{
 const browser=await chromium.launch({executablePath:'/usr/bin/chromium',headless:true,args:['--no-sandbox']});
 try {
 const page=await browser.newPage();
 // Test fixture only. This is never loaded by the Android app.
 await page.setContent(`<div id="query-builder"><div class="rules-group-body"><div class="rules-list"><div class="rule-container"><select aria-label="Filter field"><option>DestinationID</option></select><select aria-label="Filter operator"><option>equal</option></select><input aria-label="Filter value" value="91"></div></div></div></div><table id="lhTable"><thead><tr><th>My call</th><th>Destination</th><th>Time</th><th>Duration</th></tr></thead><tbody><tr class="lh-row-active"><td><span title="MY"></span>9W3SPY [Test]</td><td>World-wide (91)</td><td>now</td><td>0:03</td></tr><tr><td>TEST2</td><td>Other (46020)</td><td>1 m</td><td>0:05</td></tr><tr><td>TEST3</td><td>World-wide (91)</td><td>1 m</td><td>0:05</td></tr></tbody></table>`);
 await page.evaluate(()=>{window.__qsoFilteredTg=91;String.prototype.matchAll=undefined;NodeList.prototype.forEach=undefined});
 const result=await page.evaluate(fs.readFileSync('/root/app/src/main/assets/lastheard.js','utf8')+'(91)');
 if(result.rows.length!==2||result.rows.some(r=>r.tg!==91)||!result.rows[0].active||result.rows[0].country!=='MY')throw Error(JSON.stringify(result));
 console.log('PASS: legacy extractor without matchAll/NodeList.forEach; TG isolation, active flag, country, history');
 }finally{await browser.close()}
})().catch(e=>{console.error(e);process.exitCode=1});
