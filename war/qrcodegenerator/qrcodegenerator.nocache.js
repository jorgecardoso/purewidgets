function qrcodegenerator(){var N='',ub='" for "gwt:onLoadErrorFn"',sb='" for "gwt:onPropertyErrorFn"',gb='"><\/script>',X='#',Eb='.cache.html',Z='/',Db=':',mb='::',Pb='<script defer="defer">qrcodegenerator.onInjectionDone(\'qrcodegenerator\')<\/script>',fb='<script id="',Nb='<script language="javascript" src="',pb='=',Y='?',rb='Bad handler "',Cb='CCBCD44162FCAD4849AA2D3D20D11404',Lb='DOMContentLoaded',hb='SCRIPT',eb='__gwt_marker_qrcodegenerator',ib='base',ab='baseUrl',R='begin',Q='bootstrap',_='clear.cache.gif',ob='content',W='end',S='gwt.codesvr=',T='gwt.hosted=',U='gwt.hybrid',Fb='gwt/standard/standard.css',tb='gwt:onLoadErrorFn',qb='gwt:onPropertyErrorFn',nb='gwt:property',Kb='head',Ab='hosted.html?qrcodegenerator',Jb='href',vb='iframe',$='img',wb="javascript:''",Gb='link',zb='loadExternalRefs',jb='meta',yb='moduleRequested',V='moduleStartup',kb='name',xb='position:absolute;width:0;height:0;border:none',O='qrcodegenerator',cb='qrcodegenerator.nocache.js',lb='qrcodegenerator::',Hb='rel',bb='script',Bb='selectingPermutation',P='startup',Ib='stylesheet',Mb='swfobject.js',Ob='swfobject.js"><\/script>',db='undefined';var l=window,m=document,n=l.__gwtStatsEvent?function(a){return l.__gwtStatsEvent(a)}:null,o=l.__gwtStatsSessionId?l.__gwtStatsSessionId:null,p,q,r,s=N,t={},u=[],v=[],w=[],x=0,y,z;n&&n({moduleName:O,sessionId:o,subSystem:P,evtGroup:Q,millis:(new Date).getTime(),type:R});if(!l.__gwt_stylesLoaded){l.__gwt_stylesLoaded={}}if(!l.__gwt_scriptsLoaded){l.__gwt_scriptsLoaded={}}function A(){var b=false;try{var c=l.location.search;return (c.indexOf(S)!=-1||(c.indexOf(T)!=-1||l.external&&l.external.gwtOnLoad))&&c.indexOf(U)==-1}catch(a){}A=function(){return b};return b}
function B(){if(p&&q){var b=m.getElementById(O);var c=b.contentWindow;if(A()){c.__gwt_getProperty=function(a){return F(a)}}qrcodegenerator=null;c.gwtOnLoad(y,O,s,x);n&&n({moduleName:O,sessionId:o,subSystem:P,evtGroup:V,millis:(new Date).getTime(),type:W})}}
function C(){function e(a){var b=a.lastIndexOf(X);if(b==-1){b=a.length}var c=a.indexOf(Y);if(c==-1){c=a.length}var d=a.lastIndexOf(Z,Math.min(c,b));return d>=0?a.substring(0,d+1):N}
function f(a){if(a.match(/^\w+:\/\//)){}else{var b=m.createElement($);b.src=a+_;a=e(b.src)}return a}
function g(){var a=E(ab);if(a!=null){return a}return N}
function h(){var a=m.getElementsByTagName(bb);for(var b=0;b<a.length;++b){if(a[b].src.indexOf(cb)!=-1){return e(a[b].src)}}return N}
function i(){var a;if(typeof isBodyLoaded==db||!isBodyLoaded()){var b=eb;var c;m.write(fb+b+gb);c=m.getElementById(b);a=c&&c.previousSibling;while(a&&a.tagName!=hb){a=a.previousSibling}if(c){c.parentNode.removeChild(c)}if(a&&a.src){return e(a.src)}}return N}
function j(){var a=m.getElementsByTagName(ib);if(a.length>0){return a[a.length-1].href}return N}
var k=g();if(k==N){k=h()}if(k==N){k=i()}if(k==N){k=j()}if(k==N){k=e(m.location.href)}k=f(k);s=k;return k}
function D(){var b=document.getElementsByTagName(jb);for(var c=0,d=b.length;c<d;++c){var e=b[c],f=e.getAttribute(kb),g;if(f){f=f.replace(lb,N);if(f.indexOf(mb)>=0){continue}if(f==nb){g=e.getAttribute(ob);if(g){var h,i=g.indexOf(pb);if(i>=0){f=g.substring(0,i);h=g.substring(i+1)}else{f=g;h=N}t[f]=h}}else if(f==qb){g=e.getAttribute(ob);if(g){try{z=eval(g)}catch(a){alert(rb+g+sb)}}}else if(f==tb){g=e.getAttribute(ob);if(g){try{y=eval(g)}catch(a){alert(rb+g+ub)}}}}}}
function E(a){var b=t[a];return b==null?null:b}
function F(a){var b=v[a](),c=u[a];if(b in c){return b}var d=[];for(var e in c){d[c[e]]=e}if(z){z(a,d,b)}throw null}
var G;function H(){if(!G){G=true;var a=m.createElement(vb);a.src=wb;a.id=O;a.style.cssText=xb;a.tabIndex=-1;m.body.appendChild(a);n&&n({moduleName:O,sessionId:o,subSystem:P,evtGroup:V,millis:(new Date).getTime(),type:yb});a.contentWindow.location.replace(s+J)}}
qrcodegenerator.onScriptLoad=function(){if(G){q=true;B()}};qrcodegenerator.onInjectionDone=function(){p=true;n&&n({moduleName:O,sessionId:o,subSystem:P,evtGroup:zb,millis:(new Date).getTime(),type:W});B()};D();C();var I;var J;if(A()){if(l.external&&(l.external.initModule&&l.external.initModule(O))){l.location.reload();return}J=Ab;I=N}n&&n({moduleName:O,sessionId:o,subSystem:P,evtGroup:Q,millis:(new Date).getTime(),type:Bb});if(!A()){try{I=Cb;var K=I.indexOf(Db);if(K!=-1){x=Number(I.substring(K+1));I=I.substring(0,K)}J=I+Eb}catch(a){return}}var L;function M(){if(!r){r=true;if(!__gwt_stylesLoaded[Fb]){var a=m.createElement(Gb);__gwt_stylesLoaded[Fb]=a;a.setAttribute(Hb,Ib);a.setAttribute(Jb,s+Fb);m.getElementsByTagName(Kb)[0].appendChild(a)}B();if(m.removeEventListener){m.removeEventListener(Lb,M,false)}if(L){clearInterval(L)}}}
if(m.addEventListener){m.addEventListener(Lb,function(){H();M()},false)}var L=setInterval(function(){if(/loaded|complete/.test(m.readyState)){H();M()}},50);n&&n({moduleName:O,sessionId:o,subSystem:P,evtGroup:Q,millis:(new Date).getTime(),type:W});n&&n({moduleName:O,sessionId:o,subSystem:P,evtGroup:zb,millis:(new Date).getTime(),type:R});if(!__gwt_scriptsLoaded[Mb]){__gwt_scriptsLoaded[Mb]=true;document.write(Nb+s+Ob)}m.write(Pb)}
qrcodegenerator();