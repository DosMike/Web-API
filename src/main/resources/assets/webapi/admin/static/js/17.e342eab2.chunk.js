webpackJsonp([17],{1007:function(e,t,n){"use strict";Object.defineProperty(t,"__esModule",{value:!0});var r=n(0),o=(n.n(r),n(70)),i=n(71),a=n(60),s=n(34),l=n(144),p=n(90),c=n(145),u=n(1025),h=this&&this.__extends||function(){var e=Object.setPrototypeOf||{__proto__:[]}instanceof Array&&function(e,t){e.__proto__=t}||function(e,t){for(var n in t)t.hasOwnProperty(n)&&(e[n]=t[n])};return function(t,n){function r(){this.constructor=t}e(t,n),t.prototype=null===n?Object.create(n):(r.prototype=n.prototype,new r)}}(),d=Object(u.a)("entity","uuid"),f=function(e){function t(){return null!==e&&e.apply(this,arguments)||this}return h(t,e),t.prototype.componentDidMount=function(){this.props.requestWorlds(),this.props.requestCatalog(c.a.Entity)},t.prototype.render=function(){var e=this.props.t;return r.createElement(d,{canDelete:!0,icon:"paw",title:e("Entities"),filterTitle:e("FilterEntities"),createTitle:e("SpawnEntity"),fields:{"type.name":{label:e("Type"),create:!0,createName:"type",filter:!0,filterName:"type.id",view:function(e){return e.type.name},options:Object(p.f)(this.props.entTypes)},world:{label:e("World"),view:!1,create:!0,filter:!0,filterName:"location.world.uuid",options:Object(p.g)(this.props.worlds),required:!0},position:{label:e("Location"),isGroup:!0,view:function(e){return r.createElement(a.b,{color:"blue"},r.createElement(a.h,{name:"globe"}),e.location.world.name,"\xa0 \xa0",e.location.position.x.toFixed(0)," |\xa0",e.location.position.y.toFixed(0)," |\xa0",e.location.position.z.toFixed(0))},create:function(t){return r.createElement(a.e.Group,{inline:!0},r.createElement("label",null,e("Position")),r.createElement(a.e.Input,{type:"number",width:6,name:"position.x",placeholder:"X",value:t.state["position.x"],onChange:t.handleChange}),r.createElement(a.e.Input,{type:"number",width:6,name:"position.y",placeholder:"Y",value:t.state["position.y"],onChange:t.handleChange}),r.createElement(a.e.Input,{type:"number",width:6,name:"position.z",placeholder:"Z",value:t.state["position.z"],onChange:t.handleChange}))}},health:{label:e("Health"),wide:!0,view:function(e){if(e.health)return r.createElement(a.r,{progress:!0,color:"red",percent:Object(p.b)(e.health.current,e.health.max)})}},info:{label:e("Info"),wide:!0,view:function(t){return r.createElement("div",null,t.aiEnabled&&r.createElement(a.k,null,e("AI")),t.age&&r.createElement(a.k,null,e("Age"),r.createElement(a.k.Detail,null,t.age.adult?e("Adult"):t.age.age)),t.breedable&&r.createElement(a.k,null,e("Breedable")),t.career&&r.createElement(a.k,null,e("Career"),r.createElement(a.k.Detail,null,t.career.name)),t.flying&&r.createElement(a.k,null,e("Flying")),t.glowing&&r.createElement(a.k,null,e("Glowing")),t.silent&&r.createElement(a.k,null,e("Silent")),t.sneaking&&r.createElement(a.k,null,e("Sneaking")),t.sprinting&&r.createElement(a.k,null,e("Sprinting")))}}}})},t}(r.Component),m=function(e){return{worlds:e.world.list,entTypes:e.api.types[c.a.Entity]}},g=function(e){return{requestWorlds:function(){return e(Object(l.f)("world",!0))},requestCatalog:function(t){return e(Object(s.e)(t))}}};t.default=Object(i.b)(m,g)(Object(o.c)("Entities")(f))},1021:function(e,t,n){"use strict";var r=n(24),o=(n.n(r),n(0)),i=(n.n(o),n(70)),a=n(60),s=n(90),l=n(1022),p=n(1023),c=n(1024),u=this&&this.__extends||function(){var e=Object.setPrototypeOf||{__proto__:[]}instanceof Array&&function(e,t){e.__proto__=t}||function(e,t){for(var n in t)t.hasOwnProperty(n)&&(e[n]=t[n])};return function(t,n){function r(){this.constructor=t}e(t,n),t.prototype=null===n?Object.create(n):(r.prototype=n.prototype,new r)}}(),h=function(e){function t(t){var n=e.call(this,t)||this;return n.state={page:0,newData:{}},n.changePage=n.changePage.bind(n),n.doHandleChange=n.doHandleChange.bind(n),n.handleChange=s.e.bind(n,n.doHandleChange),n}return u(t,e),t.prototype.doHandleChange=function(e,t){this.setState({newData:r.assign({},this.state.newData,(n={},n[e]=t,n))});var n},t.prototype.changePage=function(e,t){e.preventDefault(),this.setState({page:t})},t.prototype.onEdit=function(e,t){var n={};e&&r.each(this.props.fields,function(t,o){t.edit&&(n[o]=r.get(e,o))}),this.setState({newData:n}),this.props.onEdit&&this.props.onEdit(e,t)},t.prototype.shouldComponentUpdate=function(e,t){return e.fields!==this.props.fields||e.list!==this.props.list||t.page!==this.state.page||t.newData!==this.state.newData},t.prototype.render=function(){var e=this,t=this.props,n=t.icon,i=t.title,s=t.list,u=t.canEdit,h=t.canDelete,d=t.actions,f=r.filter(this.props.fields,"view"),m=Math.ceil(s.length/20),g=Math.min(this.state.page,m-1),y=s.slice(20*g,20*(g+1)),b={handleChange:this.handleChange,state:this.state.newData,setState:function(t){return e.setState({newData:r.assign({},e.state.newData,t)})}},v=this.props.t;return o.createElement("div",{style:{marginTop:"2em"}},i&&o.createElement(a.g,null,o.createElement(a.h,{fitted:!0,name:n})," ",i),o.createElement(a.w,{striped:!0,stackable:!0},o.createElement(p.a,{fields:f,hasActions:"undefined"!==typeof d,canEdit:u,canDelete:h,t:v}),o.createElement(a.w.Body,null,r.map(y,function(t,n){return o.createElement(c.a,{key:e.props.idFunc(t),obj:t,tableRef:b,canEdit:u,canDelete:h,editing:e.props.isEditing(t),fields:f,onEdit:function(t,n){return e.onEdit(t,n)},onSave:e.props.onSave,onDelete:e.props.onDelete,actions:e.props.actions,newData:e.state.newData,handleChange:e.handleChange,t:v})}))),o.createElement(l.a,{page:g,maxPage:m,changePage:function(t,n){return e.changePage(t,n)}}))},t}(o.Component);t.a=Object(i.c)("DataTable")(h)},1022:function(e,t,n){"use strict";var r=n(0),o=(n.n(r),n(60)),i=this&&this.__extends||function(){var e=Object.setPrototypeOf||{__proto__:[]}instanceof Array&&function(e,t){e.__proto__=t}||function(e,t){for(var n in t)t.hasOwnProperty(n)&&(e[n]=t[n])};return function(t,n){function r(){this.constructor=t}e(t,n),t.prototype=null===n?Object.create(n):(r.prototype=n.prototype,new r)}}(),a=function(e){function t(){return null!==e&&e.apply(this,arguments)||this}return i(t,e),t.prototype.shouldComponentUpdate=function(e,t){return e.page!==this.props.page||e.maxPage!==this.props.maxPage},t.prototype.render=function(){var e=this;if(this.props.maxPage<=1)return null;for(var t=this.props,n=t.page,i=t.maxPage,a=Math.max(0,n-4),s=Math.min(i,n+5),l=[],p=a;p<s;p++)l.push(p);return r.createElement(o.n,{pagination:!0},n>4?r.createElement(o.n.Item,{onClick:function(t){return e.props.changePage(t,0)}},"1"):null,n>5?r.createElement(o.n.Item,{onClick:function(t){return e.props.changePage(t,n-5)}},"..."):null,l.map(function(t){return r.createElement(o.n.Item,{key:t,onClick:function(n){return e.props.changePage(n,t)},active:t===n},t+1)}),n<i-6?r.createElement(o.n.Item,{onClick:function(t){return e.props.changePage(t,n+5)}},"..."):null,n<i-5?r.createElement(o.n.Item,{onClick:function(t){return e.props.changePage(t,i-1)}},i):null)},t}(r.Component);t.a=a},1023:function(e,t,n){"use strict";var r=n(0),o=(n.n(r),n(60)),i=this&&this.__extends||function(){var e=Object.setPrototypeOf||{__proto__:[]}instanceof Array&&function(e,t){e.__proto__=t}||function(e,t){for(var n in t)t.hasOwnProperty(n)&&(e[n]=t[n])};return function(t,n){function r(){this.constructor=t}e(t,n),t.prototype=null===n?Object.create(n):(r.prototype=n.prototype,new r)}}(),a=function(e){function t(){return null!==e&&e.apply(this,arguments)||this}return i(t,e),t.prototype.shouldComponentUpdate=function(e,t){return e.fields!==this.props.fields},t.prototype.render=function(){var e=this;return r.createElement(o.w.Header,null,r.createElement(o.w.Row,null,Object.keys(this.props.fields).map(function(t){var n=e.props.fields[t];return r.createElement(o.w.HeaderCell,{key:t},n.label?n.label:"<"+n.name+">")}),this.props.hasActions||this.props.canEdit||this.props.canDelete?r.createElement(o.w.HeaderCell,null,this.props.t("Actions")):null))},t}(r.Component);t.a=a},1024:function(e,t,n){"use strict";var r=n(0),o=(n.n(r),n(60)),i=n(90),a=this&&this.__extends||function(){var e=Object.setPrototypeOf||{__proto__:[]}instanceof Array&&function(e,t){e.__proto__=t}||function(e,t){for(var n in t)t.hasOwnProperty(n)&&(e[n]=t[n])};return function(t,n){function r(){this.constructor=t}e(t,n),t.prototype=null===n?Object.create(n):(r.prototype=n.prototype,new r)}}(),s=function(e){function t(){return null!==e&&e.apply(this,arguments)||this}return a(t,e),t.prototype.shouldComponentUpdate=function(e,t){return e.obj!==this.props.obj||e.editing!==this.props.editing||e.fields!==this.props.fields||this.props.editing&&e.newData!==this.props.newData},t.prototype.renderEdit=function(e,t){return t.options?r.createElement(o.e.Field,{fluid:!0,selection:!0,search:!0,control:o.d,name:t.name,placeholder:t.label,options:t.options,value:this.props.newData[t.name],onChange:this.props.handleChange}):r.createElement(o.e.Input,{name:t.name,type:t.type?t.type:"text",placeholder:t.label,value:this.props.newData[t.name],onChange:this.props.handleChange})},t.prototype.render=function(){var e=this,t=this.props,n=t.actions,a=t.fields,s=t.obj,l=t.canEdit,p=t.canDelete,c=t.editing,u=t.tableRef;return r.createElement(o.w.Row,null,a.map(function(t,n){return r.createElement(o.w.Cell,{key:n,collapsing:!t.wide},t.edit&&c?"function"===typeof t.edit?t.edit(s,u):e.renderEdit(s,t):"function"===typeof t.view?t.view(s,u):Object(i.d)(s,t.name))}),n||l||p?r.createElement(o.w.Cell,{collapsing:!0},l&&c?[r.createElement(o.b,{key:"save",color:"green",disabled:s.updating,loading:s.updating,onClick:function(){e.props.onSave&&e.props.onSave(s,e.props.newData,u)}},r.createElement(o.h,{name:"save"})," ",this.props.t("Save")),r.createElement(o.b,{key:"cancel",color:"yellow",disabled:s.updating,loading:s.updating,onClick:function(){return e.props.onEdit(null,u)}},r.createElement(o.h,{name:"cancel"})," ",this.props.t("Cancel"))]:l?r.createElement(o.b,{color:"blue",disabled:s.updating,loading:s.updating,onClick:function(){return e.props.onEdit(s,u)}},r.createElement(o.h,{name:"edit"})," ",this.props.t("Edit")):null,p&&r.createElement(o.b,{color:"red",disabled:s.updating,loading:s.updating,onClick:function(){e.props.onDelete&&e.props.onDelete(s,u)}},r.createElement(o.h,{name:"trash"})," ",this.props.t("Remove")),n&&n(s,u)):null)},t}(r.Component);t.a=s},1025:function(e,t,n){"use strict";function r(e,t){return function(n,r){var o=a.get(n,e.replace(/\//g,"_").replace("-",""));return{creating:!!o&&o.creating,filter:o&&o.filter?o.filter:{},list:o?o.list:[],types:n.api.types,idFunc:t,perm:e.split("/"),perms:n.api.permissions}}}function o(e,t,n){return function(r){return{requestList:function(){return r(Object(c.f)(e,!n))},requestDetails:function(n){return r(Object(c.e)(e,t,n))},requestCreate:function(n){return r(Object(c.c)(e,t,n))},requestChange:function(n,o){return r(Object(c.b)(e,t,n,o))},requestDelete:function(n){return r(Object(c.d)(e,t,n))},setFilter:function(t,n){return r(Object(c.l)(e,t,n))},equals:function(e,n){return null!=e&&null!=n&&t(e)===t(n)}}}}function i(e,t,n){t||(t="id");var i="function"===typeof t?t:function(e){return a.get(e,t)};return Object(l.b)(r(e,i),o(e,i,!!n))(g)}t.a=i;var a=n(24),s=(n.n(a),n(0)),l=(n.n(s),n(71)),p=n(60),c=n(144),u=n(1026),h=n(1021),d=n(1027),f=n(90),m=this&&this.__extends||function(){var e=Object.setPrototypeOf||{__proto__:[]}instanceof Array&&function(e,t){e.__proto__=t}||function(e,t){for(var n in t)t.hasOwnProperty(n)&&(e[n]=t[n])};return function(t,n){function r(){this.constructor=t}e(t,n),t.prototype=null===n?Object.create(n):(r.prototype=n.prototype,new r)}}(),g=function(e){function t(t){var n=e.call(this,t)||this;return n.state={page:0,data:null},n.details=n.details.bind(n),n.create=n.create.bind(n),n.edit=n.edit.bind(n),n.save=n.save.bind(n),n.delete=n.delete.bind(n),n}return m(t,e),t.prototype.createTable=function(){return h.a},t.prototype.componentDidMount=function(){this.props.static||(this.props.requestList(),this.interval=setInterval(this.props.requestList,1e4))},t.prototype.componentWillUnmount=function(){this.interval&&clearInterval(this.interval)},t.prototype.shouldComponentUpdate=function(e,t){return e.creating!==this.props.creating||e.filter!==this.props.filter||e.fields!==this.props.fields||e.list!==this.props.list||t.data!==this.state.data},t.prototype.create=function(e){this.props.requestCreate(e)},t.prototype.details=function(e){this.props.requestDetails(e)},t.prototype.edit=function(e){this.setState({data:e})},t.prototype.save=function(e,t){this.props.requestChange(e,t),this.setState({data:null})},t.prototype.delete=function(e){this.props.requestDelete(e)},t.prototype.render=function(){var e=this,t=[],n=!1,r=function(t){return a.assign({},t,{create:e.create,details:e.details,save:e.save,edit:e.edit,delete:e.delete})},o=a.mapValues(this.props.fields,function(e,t){var n={name:t,view:!0};if("string"===typeof e)n.label=e;else if("function"===typeof e)n.view=function(t,n){return e(t,r(n))};else if("object"===typeof e&&(a.assign(n,e),"function"===typeof e.view)){var o=e.view;n.view=function(e,t){return o(e,r(t))}}return n}),i={},l={};a.each(o,function(e,t){e.create&&(i[t]=e),e.filter&&(l[t]=e)});try{a.each(this.props.filter,function(e,n){var r=a.find(l,{filterName:n})||l[n],o=function(e){return a.get(e,n)};if("function"===typeof r.filterValue&&(o=r.filterValue),a.isArray(e)){if(0===e.length)return;t.push(function(t){var n=o(t);return e.indexOf(n)>=0})}else t.push(function(t){return new RegExp(e,"i").test(o(t))})}),n=!0}catch(e){n=!1}var c=a.filter(this.props.list,function(e){return!n||a.every(t,function(t){return t(e)})}),h=this.props.createTitle&&this.props.filterTitle?2:1,m=this.props.actions,g=m;"function"===typeof m&&(g=function(e,t){return m(e,r(t))});var y=this.createTable();return s.createElement(p.t,{basic:!0},s.createElement(p.f,{stackable:!0,doubling:!0,columns:h},this.props.createTitle&&Object(f.a)(this.props.perms,this.props.perm.concat("create"))&&s.createElement(p.f.Column,null,s.createElement(u.a,{title:this.props.createTitle,button:this.props.createButton,creating:this.props.creating,fields:i,onCreate:function(t,n){return e.props.onCreate?e.props.onCreate(t,r(n)):e.create(t)}})),this.props.filterTitle&&s.createElement(p.f.Column,null,s.createElement(d.a,{title:this.props.filterTitle,fields:l,valid:n,values:this.props.filter,onFilterChange:this.props.setFilter}))),s.createElement(y,{title:this.props.title,icon:this.props.icon,list:c,t:this.props.t,idFunc:this.props.idFunc,fields:o,onEdit:function(t,n){return e.props.onEdit?e.props.onEdit(t,r(n)):e.edit(t)},onSave:function(t,n,o){return e.props.onSave?e.props.onSave(t,n,r(o)):e.save(t,n)},onDelete:function(t,n){return e.props.onDelete?e.props.onDelete(t,r(n)):e.delete(t)},canEdit:Object(f.a)(this.props.perms,this.props.perm.concat("change"))&&this.props.canEdit,canDelete:Object(f.a)(this.props.perms,this.props.perm.concat("delete"))&&this.props.canDelete,actions:g,isEditing:function(t){return e.props.equals(t,e.state.data)}}))},t}(s.Component)},1026:function(e,t,n){"use strict";var r=n(24),o=(n.n(r),n(0)),i=(n.n(o),n(70)),a=n(60),s=n(90),l=this&&this.__extends||function(){var e=Object.setPrototypeOf||{__proto__:[]}instanceof Array&&function(e,t){e.__proto__=t}||function(e,t){for(var n in t)t.hasOwnProperty(n)&&(e[n]=t[n])};return function(t,n){function r(){this.constructor=t}e(t,n),t.prototype=null===n?Object.create(n):(r.prototype=n.prototype,new r)}}(),p=function(e){function t(t){var n=e.call(this,t)||this;return n.state={newData:{}},n.doHandleChange=n.doHandleChange.bind(n),n.handleChange=s.e.bind(n,n.doHandleChange),n.create=n.create.bind(n),n}return l(t,e),t.prototype.doHandleChange=function(e,t){this.setState({newData:r.assign({},this.state.newData,(n={},n[e]=t,n))});var n},t.prototype.shouldComponentUpdate=function(e,t){return e.creating!==this.props.creating||e.fields!==this.props.fields||t.newData!==this.state.newData},t.prototype.create=function(){var e=this,t={};Object.keys(this.state.newData).forEach(function(n){r.set(t,n,e.state.newData[n])}),this.props.onCreate(t,{state:this.state.newData,setState:this.setState,handleChange:this.handleChange})},t.prototype.canCreate=function(){var e=this;return Object.keys(this.props.fields).every(function(t){var n=e.props.fields[t],r=n.createName?n.createName:t;return"string"===typeof n||!n.required||e.state.newData[r]})},t.prototype.render=function(){var e=this,t=this.props,n=t.title,i=t.creating,s=t.fields,l=this.props.t,p=[];return Object.keys(s).forEach(function(e){var t=s[e],n=r.assign({},t,{name:t.createName?t.createName:e});n.isGroup?p.push({only:n}):p.length&&!p[p.length-1].second?p[p.length-1].second=n:p.push({first:n})}),o.createElement(a.t,null,o.createElement(a.g,null,o.createElement(a.h,{fitted:!0,name:"plus"})," ",n),o.createElement(a.e,{loading:i},p.map(function(t,n){return t.only?o.createElement("div",{key:n},e.renderField(t.only)):o.createElement(a.e.Group,{key:n,widths:"equal"},t.first&&e.renderField(t.first),t.second&&e.renderField(t.second))}),o.createElement(a.b,{color:"green",onClick:this.create,disabled:!this.canCreate()},this.props.button||l("Create"))))},t.prototype.renderField=function(e){var t=this.state.newData;return"function"===typeof e.create?e.create({state:t,setState:this.setState,handleChange:this.handleChange,value:t[e.name]}):e.options?o.createElement(a.e.Field,{fluid:!0,selection:!0,search:!0,required:e.required,control:a.d,name:e.name,label:e.label,placeholder:e.label,onChange:this.handleChange,value:t[e.name],options:e.options}):o.createElement(a.e.Input,{required:e.required,type:e.type?e.type:"text",name:e.name,label:e.label,placeholder:e.label,onChange:this.handleChange,value:t[e.name]})},t}(o.Component);t.a=Object(i.c)("CreateForm")(p)},1027:function(e,t,n){"use strict";var r=n(24),o=(n.n(r),n(0)),i=(n.n(o),n(60)),a=n(90),s=this&&this.__extends||function(){var e=Object.setPrototypeOf||{__proto__:[]}instanceof Array&&function(e,t){e.__proto__=t}||function(e,t){for(var n in t)t.hasOwnProperty(n)&&(e[n]=t[n])};return function(t,n){function r(){this.constructor=t}e(t,n),t.prototype=null===n?Object.create(n):(r.prototype=n.prototype,new r)}}(),l=function(e){function t(t){var n=e.call(this,t)||this;return n.handleChange=a.e.bind(n,n.props.onFilterChange),n}return s(t,e),t.prototype.shouldComponentUpdate=function(e,t){return e.values!==this.props.values||e.fields!==this.props.fields||e.valid!==this.props.valid},t.prototype.render=function(){var e=this,t=this.props,n=t.title,a=t.fields,s=t.values,l=t.valid,p=[];return Object.keys(a).forEach(function(e){var t=a[e],n=r.assign({},t,{name:t.filterName?t.filterName:e});n.isGroup?p.push({only:n}):p.length&&!p[p.length-1].second?p[p.length-1].second=n:p.push({first:n})}),o.createElement(i.t,null,o.createElement(i.g,null,o.createElement(i.h,{name:"filter",fitted:!0})," ",n),o.createElement(i.e,null,p.map(function(t,n){return t.only?e.renderField(t.only,r.get(s,t.only.name),!l):o.createElement(i.e.Group,{key:n,widths:"equal"},t.first&&e.renderField(t.first,r.get(s,t.first.name),!l),t.second&&e.renderField(t.second,r.get(s,t.second.name),!l))}),o.createElement(i.o,{error:!0,visible:!l,content:"Search term must be a valid regex"})))},t.prototype.renderField=function(e,t,n){return"function"===typeof e.filter?e.filter({state:this.props.values,setState:this.setState,handleChange:this.handleChange,value:t}):e.options?(t||(t=[]),o.createElement(i.e.Field,{fluid:!0,selection:!0,search:!0,multiple:!0,control:i.d,name:e.name,label:e.label,placeholder:e.label,options:e.options,value:t,error:n,onChange:this.handleChange})):o.createElement(i.e.Input,{name:e.name,type:e.type?e.type:"text",label:e.label,placeholder:e.label,value:t,error:n,onChange:this.handleChange})},t}(o.Component);t.a=l}});
//# sourceMappingURL=17.e342eab2.chunk.js.map