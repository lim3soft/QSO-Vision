// Read-only CDP inspection of debug WebViews; no network protocol reverse engineering.
const expression = process.argv[3] || 'JSON.stringify({text:document.body.innerText.slice(0,2200),visibility:document.visibilityState,ua:navigator.userAgent})';
(async()=>{
  const pages=await (await fetch('http://127.0.0.1:'+(process.argv[2]||9222)+'/json')).json();
  for(const page of pages){
    console.log('PAGE',page.url);
    await new Promise((resolve,reject)=>{
      const socket=new WebSocket(page.webSocketDebuggerUrl);
      const timeout=setTimeout(()=>{socket.close();reject(Error('CDP timeout'))},15000);
      socket.onopen=()=>socket.send(JSON.stringify({id:1,method:'Runtime.evaluate',params:{expression,returnByValue:true}}));
      socket.onmessage=e=>{const r=JSON.parse(e.data);if(r.id===1){console.log(JSON.stringify(r.result));clearTimeout(timeout);socket.close();resolve()}};
      socket.onerror=reject;
    });
  }
})().catch(e=>{console.error(e);process.exitCode=1});
