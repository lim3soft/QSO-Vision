const {chromium}=require('playwright');
const fs=require('fs');
(async()=>{
 const browser=await chromium.launch({executablePath:'/usr/bin/chromium',headless:true,args:['--no-sandbox']});
 const page=await browser.newPage({viewport:{width:1080,height:1800}});
 await page.goto('https://brandmeister.network/#/lh',{waitUntil:'domcontentloaded',timeout:60000});
 const script=fs.readFileSync('/root/app/src/main/assets/lastheard.js','utf8');
 for (const tg of [91,46020,91]) {
  let passed=false;
  for(let i=0;i<35;i++){
   const result=await page.evaluate(script+'('+tg+')');
   if(result.rows.length){
    if(result.rows.some(r=>r.tg!==tg))throw Error('Wrong TG returned');
    console.log(JSON.stringify({tg,records:result.rows.length,first:result.rows[0],active:result.rows.filter(r=>r.active)}));
    passed=true;break;
   }
   await page.waitForTimeout(1000);
  }
  if(!passed) throw Error('No source results for TG '+tg);
 }
 await browser.close();
})().catch(e=>{console.error(e);process.exitCode=1});
