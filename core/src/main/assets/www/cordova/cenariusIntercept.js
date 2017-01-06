//lastXmlhttpRequestPrototypeMethod = null;
//lastXmlhttpRequestPrototypeHeader = null;
//
//XMLHttpRequest.prototype.reallyOpen = XMLHttpRequest.prototype.open;
//XMLHttpRequest.prototype.open = function (method, url, async, user, password) {
//    lastXmlhttpRequestPrototypeMethod = method;
//    this.reallyOpen(method, url, async, user, password);
//};
//
//XMLHttpRequest.prototype.reallySend = XMLHttpRequest.prototype.send;
//XMLHttpRequest.prototype.send = function (body) {
//    window.cenariusInterception.customAjax(lastXmlhttpRequestPrototypeMethod, JSON.stringify(lastXmlhttpRequestPrototypeHeader), body);
//    lastXmlhttpRequestPrototypeMethod = null;
//    lastXmlhttpRequestPrototypeHeader = null;
//    this.reallySend(body);
//};
//
//XMLHttpRequest.prototype.reallySetRequestHeader = XMLHttpRequest.prototype.setRequestHeader;
//XMLHttpRequest.prototype.setRequestHeader = function (name, value) {
//    if (!lastXmlhttpRequestPrototypeHeader) {
//        lastXmlhttpRequestPrototypeHeader = {};
//    }
//    lastXmlhttpRequestPrototypeHeader[name] = value;
//    this.reallySetRequestHeader(name, value);
//};