package com.sblorftz.timer.remote

/** Pagina web servita al telefono: struttura modificabile, salvataggio in un colpo solo. */
internal object StructurePage {
    private const val STYLE = """
:root{--gold:#D4AF37;--gold-l:#F3D77A;--cream:#F4EFE3;--muted:#9AA69F;--red:#E5484D;--felt:#13261D;--dark:#0A120E}
*{box-sizing:border-box;margin:0;padding:0;-webkit-tap-highlight-color:transparent}
body{font-family:-apple-system,BlinkMacSystemFont,"Segoe UI",Roboto,Inter,sans-serif;color:var(--cream);
  background:radial-gradient(ellipse at 50% 0%,#1d3a2c 0%,var(--felt) 40%,#050706 100%) fixed;min-height:100vh;padding:20px 14px 150px}
header{max-width:640px;margin:0 auto 18px}
.brand{font-weight:800;letter-spacing:.35em;color:var(--gold);font-size:13px}
.brand span{color:rgba(244,239,227,.35);letter-spacing:.2em;margin-left:6px}
h1{font-size:40px;font-weight:900;margin-top:8px;letter-spacing:-.01em}
.sub{color:var(--muted);font-size:14px;margin-top:4px}
ol{list-style:none;max-width:640px;margin:0 auto;display:flex;flex-direction:column;gap:8px}
li{display:flex;align-items:center;gap:8px;background:rgba(255,255,255,.04);border:1px solid rgba(255,255,255,.07);border-radius:14px;padding:10px}
li.cur{border-color:var(--gold)}
li.chip{background:rgba(212,175,55,.08)}
.n{width:30px;text-align:center;font-weight:800;font-size:20px;color:var(--cream)}
li.cur .n{color:var(--gold)}
.main{flex:1;min-width:0;display:flex;align-items:center;gap:6px}
.chipname{color:var(--gold-l);font-weight:800;letter-spacing:.08em;font-size:14px}
input{background:rgba(0,0,0,.35);border:1px solid rgba(255,255,255,.12);color:var(--cream);border-radius:10px;
  font:inherit;font-weight:700;font-size:18px;padding:8px;width:100%;min-width:0;-moz-appearance:textfield}
input:focus{outline:none;border-color:var(--gold)}
input::-webkit-outer-spin-button,input::-webkit-inner-spin-button{-webkit-appearance:none}
.sb{flex:1}.bb{color:var(--muted);font-weight:700;font-size:15px;white-space:nowrap;min-width:64px}
.min{width:62px;flex:none;text-align:center}.u{color:var(--muted);font-size:13px}
.tools{display:flex;gap:4px}
button{font:inherit;cursor:pointer;border-radius:10px;border:1px solid rgba(212,175,55,.35);background:transparent;color:var(--cream)}
.ic{width:34px;height:38px;font-size:15px;padding:0}
.ic.x{border-color:rgba(229,72,77,.4);color:var(--red)}
.ic:disabled{opacity:.25}
.tag{font-size:10px;letter-spacing:.2em;color:var(--gold);font-weight:700;display:block}
footer{position:fixed;left:0;right:0;bottom:0;padding:12px 14px calc(12px + env(safe-area-inset-bottom));
  background:linear-gradient(transparent,rgba(5,7,6,.95) 30%)}
.bar{max-width:640px;margin:0 auto;display:grid;grid-template-columns:1fr 1fr;gap:8px}
.bar button{padding:13px 8px;font-weight:700;letter-spacing:.08em;font-size:13px;text-transform:uppercase}
.save{grid-column:1/-1;background:var(--gold);color:var(--dark);border-color:var(--gold);font-size:16px!important}
.save:disabled{opacity:.5}
#msg{text-align:center;font-size:14px;min-height:20px;margin-bottom:6px;color:var(--muted)}
#msg.ok{color:var(--gold-l)}#msg.err{color:var(--red)}
.box{max-width:420px;margin:30vh auto 0;text-align:center}
.box p{color:var(--muted);margin-top:10px}
"""

    private const val SCRIPT = """
const T=new URLSearchParams(location.search).get('t');
let levels=[],current=null,dirty=false;
const $=s=>document.querySelector(s);
const fmt=v=>String(v).replace(/\B(?=(\d{3})+(?!\d))/g,'.');
function mins(){return levels.reduce((a,l)=>a+(+l.minutes||0),0)}
function dur(m){const h=Math.floor(m/60),r=m%60;return h?h+' h'+(r?' '+r+' min':''):r+' min'}
function msg(t,c){const m=$('#msg');m.textContent=t;m.className=c||''}
function touch(){dirty=true;msg('Modifiche non salvate');summary()}
function summary(){$('#sub').textContent=levels.filter(l=>l.smallBlind>0).length+' livelli · '+dur(mins())+' in tutto'}
function render(){
  const ol=$('#list');ol.innerHTML='';let n=0;
  levels.forEach((l,i)=>{
    const li=document.createElement('li');const chip=l.smallBlind===0;
    if(!chip)n++;if(i===current)li.classList.add('cur');if(chip)li.classList.add('chip');
    li.innerHTML='<div class="n">'+(chip?'—':n)+'</div><div class="main">'+
      (chip?'<span class="chipname">CAMBIO FICHES</span><span style="flex:1"></span>'
           :'<input class="sb" inputmode="numeric" value="'+l.smallBlind+'"><span class="bb">/ '+fmt(l.smallBlind*2)+'</span>')+
      '<input class="min" inputmode="numeric" value="'+l.minutes+'"><span class="u">min</span></div>'+
      '<div class="tools"><button class="ic up"'+(i===0?' disabled':'')+'>▲</button>'+
      '<button class="ic dn"'+(i===levels.length-1?' disabled':'')+'>▼</button><button class="ic x">✕</button></div>';
    if(i===current)li.querySelector('.n').insertAdjacentHTML('beforeend','<span class="tag">ORA</span>');
    const sb=li.querySelector('.sb');
    if(sb)sb.oninput=()=>{const v=parseInt(sb.value.replace(/\D/g,''))||0;l.smallBlind=v;li.querySelector('.bb').textContent='/ '+fmt(v*2);touch()};
    const mi=li.querySelector('.min');
    mi.oninput=()=>{l.minutes=parseInt(mi.value.replace(/\D/g,''))||0;touch()};
    li.querySelector('.up').onclick=()=>move(i,-1);
    li.querySelector('.dn').onclick=()=>move(i,1);
    li.querySelector('.x').onclick=()=>{if(levels.length>1){levels.splice(i,1);fix(i);touch();render()}};
    ol.appendChild(li)});
  summary()}
function fix(i){if(current===null)return;if(i<current)current--;else if(i===current)current=Math.min(current,levels.length-1)}
function move(i,d){const j=i+d;if(j<0||j>=levels.length)return;[levels[i],levels[j]]=[levels[j],levels[i]];
  if(current===i)current=j;else if(current===j)current=i;touch();render()}
function add(chip){
  const last=[...levels].reverse().find(l=>l.smallBlind>0);
  levels.push(chip?{smallBlind:0,minutes:5}:{smallBlind:(last?last.smallBlind:50)*2,minutes:last?last.minutes:15});
  touch();render();scrollTo({top:document.body.scrollHeight,behavior:'smooth'})}
function check(){
  if(!levels.some(l=>l.smallBlind>0))return 'Serve almeno un livello con i bui';
  for(const[i,l]of levels.entries()){
    if(l.minutes<1||l.minutes>120)return 'Riga '+(i+1)+': durata fra 1 e 120 minuti';
    if(l.smallBlind<0||l.smallBlind>1000000)return 'Riga '+(i+1)+': piccolo buio troppo alto'}
  return null}
async function save(){
  const e=check();if(e)return msg(e,'err');
  $('#save').disabled=true;msg('Salvataggio…');
  try{const r=await fetch('/api/structure?t='+T,{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify(levels)});
    if(r.status===403)return expired();
    if(!r.ok)throw 0;dirty=false;msg('Salvata sulla TV ✓','ok')}
  catch(_){msg('Errore: la TV non risponde. Sei sulla stessa wifi?','err')}
  finally{$('#save').disabled=false}}
function expired(){document.body.innerHTML='<div class="box"><h1>QR scaduto</h1><p>Riapri Struttura sulla TV e inquadra il nuovo QR.</p></div>'}
async function load(){
  try{const r=await fetch('/api/structure?t='+T,{cache:'no-store'});if(r.status===403)return expired();
    const d=await r.json();levels=d.levels;current=d.current;dirty=false;render();msg('')}
  catch(_){msg('La TV non risponde','err')}}
$('#addl').onclick=()=>add(false);$('#addc').onclick=()=>add(true);$('#save').onclick=save;
addEventListener('beforeunload',e=>{if(dirty){e.preventDefault();e.returnValue=''}});
load();
"""

    val html = """<!doctype html><html lang="it"><head><meta charset="utf-8">
<meta name="viewport" content="width=device-width,initial-scale=1,viewport-fit=cover">
<meta name="theme-color" content="#0A120E"><title>Struttura · Sblorftz Timer</title><style>$STYLE</style></head><body>
<header><div class="brand">SBLORFTZ<span>TIMER</span></div><h1>Struttura</h1><div class="sub" id="sub">Caricamento…</div></header>
<ol id="list"></ol>
<footer><div id="msg"></div><div class="bar"><button id="addl">+ Livello</button><button id="addc">+ Cambio fiches</button>
<button class="save" id="save">Salva sulla TV</button></div></footer>
<script>$SCRIPT</script></body></html>"""

    val expired = """<!doctype html><html lang="it"><head><meta charset="utf-8">
<meta name="viewport" content="width=device-width,initial-scale=1"><title>Sblorftz Timer</title><style>$STYLE</style></head>
<body><div class="box"><h1>QR scaduto</h1><p>Riapri Struttura sulla TV e inquadra il nuovo QR.</p></div></body></html>"""
}
