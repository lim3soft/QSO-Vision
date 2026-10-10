// Reload emulator debug WebViews and capture standard browser errors.
(async()=>{
 const pages=await(await fetch('http://127.0.0.1:'+(process.argv[2]||9223)+'/json')).json();
 await Promise.all(pages.map(page=>new Promise(resolve=>{
  const s=new WebSocket(page.webSocketDebuggerUrl);
  s.onopen=()=>{for(const [i,method] of ['Network.enable','Runtime.enable','Log.enable'].entries())s.send(JSON.stringify({id:i+1,method}));s.send(JSON.stringify({id:4,method:'Page.navigate',params:{url:page.url || 'https://brandmeister.network/#/lh'}}))};
  s.onmessage=e=>{const r=JSON.parse(e.data);if(['Network.loadingFailed','Runtime.exceptionThrown','Log.entryAdded'].includes(r.method))console.log(page.url,JSON.stringify(r))};
  setTimeout(()=>{s.close();resolve()},20000);
 })));
})().catch(e=>{console.error(e);process.exitCode=1});
