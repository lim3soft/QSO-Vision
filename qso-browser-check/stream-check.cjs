const {chromium}=require('playwright');
(async()=>{
 const browser=await chromium.launch({executablePath:'/usr/bin/chromium',headless:true,args:['--no-sandbox']});
 const page=await browser.newPage();
 page.on('websocket',ws=>{
  console.log('PAGE SOCKET',ws.url());
  let n=0;
  ws.on('framesent',f=>console.log('PAGE SEND',f.payload));
  ws.on('framereceived',f=>{if(n++<4) console.log('PAGE RECEIVE',String(f.payload).slice(0,1600))});
 });
 page.on('console',m=>console.log(m.text()));
 page.on('pageerror',e=>console.log('JS ERROR',e.message));
 await page.goto('https://brandmeister.network/#/lh',{waitUntil:'domcontentloaded'});
 await page.evaluate(()=>{
  const ws=new WebSocket('wss://api.brandmeister.network/lh/socket.io/?EIO=4&transport=websocket');
  let count=0;
  ws.onopen=()=>console.log('SOCKET OPEN');
  ws.onerror=()=>console.log('SOCKET ERROR');
  ws.onclose=e=>console.log('SOCKET CLOSE',e.code,e.reason);
  ws.onmessage=e=>{
   const m=e.data;
   if(count++<12) console.log('FRAME',m);
   if(m==='2')ws.send('3');
   if(m.startsWith('0'))ws.send('40');
  };
 });
 await page.waitForTimeout(35000);
 console.log('TABLE', (await page.locator('body').innerText()).slice(-5000));
 await browser.close();
})().catch(e=>{console.error(e);process.exitCode=1});
