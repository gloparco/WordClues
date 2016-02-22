var $ = (function (w, doc) {
    'use strict';

	function hasClass (el, value) {
		return new RegExp('(^|\\s)' + value + '(\\s|$)').test(el.className);
	}

	function bind (el, type, fn, capture) {
		el.addEventListener(type, fn, !!capture);
	}

	function unbind (el, type, fn, capture) {
		el.removeEventListener(type, fn, !!capture);
	}

	function prefixStyle (style) {
		style = style.charAt(0).toUpperCase() + style.substr(1);

		if ( $.vendor === '' ) return style.toLowerCase();
		return $.vendor + style;
	}

	function transition (el, value, onCompletion) {
		var transform = [],
			callback = function () {
				if ( this != el ) return;
				unbind(el, $.eventTransitionEnd, callback);
				onCompletion.call(el);
			};

		value = value || {};
		value.duration = value.duration || 0;

		if ( value.property ) el.style[ prefixStyle('transitionProperty') ] = value.property;
		if ( value.duration !== undefined ) el.style[ prefixStyle('transitionDuration') ] = value.duration + 'ms';
		if ( value.easing ) el.style[ prefixStyle('transitionTimingFunction') ] = 'cubic-bezier(' + value.easing + ')';
		if ( value.delay !== undefined ) el.style[ prefixStyle('transitionDelay') ] = value.delay + 'ms';

		if ( value.opacity !== undefined ) el.style.opacity = value.opacity;
		if ( value.scale !== undefined ) transform.push('scale(' + value.scale + ')');
		if ( value.rotate !== undefined ) transform.push('rotate(' + value.rotate + 'deg)');
		if ( value.x !== undefined || value.y !== undefined ) {
			value.x = value.x || 0;
			value.y = value.y || 0;
			transform.push('translate(' + value.x + 'px,' + value.y + 'px)' + ($.has3d ? ' translateZ(0)' : ''));
		}

		if ( transform.length ) el.style[ $.transform ] = transform.join(' ');

		if ( onCompletion ) {
			if ( !value.duration ) onCompletion.call(el);
			else bind(el, $.eventTransitionEnd, callback);
		}
	}

	function $$ (query, context) {
		query = query || doc;
		if ( query instanceof Spice ) return query;
		if ( context instanceof Spice ) context = context[0];
		if ( typeof query == 'object' && (query.nodeType || query === w) ) return [query];
		return ( context || doc ).querySelectorAll(query);
	}

	function $ (query, context) {
		return new Spice($$(query, context));
	}

	function Spice (dom) {
		for ( var i = 0, l = dom.length; i < l; i++ ) this[i] = dom[i];
		return this;
	}

	Spice.prototype = {
		length: function () {
			var s = 0, i;
			for ( i in this ) if (this.hasOwnProperty(i)) s++;
			return s;
		},

		get: function (n) {
			return $(this[n]);
		},

		each: function (fn) {
			var i = 0,
				l = this.length();

			for ( ; i < l; i++ ) fn.call(this[i]);
			return this;
		},

		className: function (value) {
			if ( value === undefined ) return this[0].className;

			return this.each(function () {
				this.className = value;
			});
		},

		hasClass: function (value) {
			return hasClass(this[0], value);
		},

		addClass: function (values) {
			var i, l;

			values = values.split(' ');
			l = values.length;

			return this.each(function () {
				for ( i = 0; i < l; i++ ) {
					if ( !hasClass(this, values[i]) ) this.className = this.className !== '' ? this.className + ' ' + values[i] : values[i];
				}
			});
		},

		removeClass: function (value) {
			// Not using regex here to get a cleaner output
			return this.each(function () {
				var classes = this.className.split(' '),
					newClasses = [],
					i = 0,
					l = classes.length;

				for ( ; i < l; i++ ) {
					if ( classes[i] != value ) newClasses.push(classes[i]);
				}

				this.className = newClasses.length ? newClasses.join(' ') : null;
			});
		},

		style: function (value) {
			if (typeof value == 'string') return w.getComputedStyle(this[0], null)[value];

			this.each(function () {
				for (var i in value) {
					this.style[i] = value[i];
				}
			});

			return this;
		},

		width: function (value) {
			if ( value === undefined ) return this[0].offsetWidth;

			return this.each(function () {
				this.style.width = value + 'px';
			});
		},

		height: function (value) {
			if ( value === undefined ) return this[0].offsetHeight;

			return this.each(function () {
				this.style.height = value + 'px';
			});
		},

		left: function () {
			return this[0].offsetLeft;
		},

		top: function () {
			return this[0].offsetTop;
		},

		offset: function () {
			var el = this[0],
				offsetX = el.offsetLeft,
				offsetY = el.offsetTop;

			while ( el = el.offsetParent ) {
				offsetX += el.offsetLeft;
				offsetY += el.offsetTop;
			}

			return {
				x: offsetX,
				y: offsetY
			};
		},

		repaint: function () {
			return this.each(function () {
				this.offsetHeight;
			});
		},

		bind: function (type, fn, capture) {
			return this.each(function () {
				bind(this, type, fn, capture);
			});
		},

		unbind: function (type, fn, capture) {
			return this.each(function () {
/*
				if ( type == 'tap' && this._tapInstance ) {
					this._tapInstance = this._tapInstance.destroy();
					delete this._tapInstance;
				}
*/
				unbind(this, type, fn, capture);
			});
		},

		append: function (el) {
			el = el instanceof Spice ? el[0] : el;
			this[0].appendChild(el);
			return this;
		},

		remove: function () {
			this.each(function () {
				this.parentNode.removeChild(this);
			});
		},

		html: function (value) {
			if ( value === undefined ) return this[0].innerHTML;

			return this.each(function () {
				this.innerHTML = value;
			});
		},

		text: function (value) {
			if ( value === undefined ) return this[0].innerText;

			return this.each(function () {
				this.innerText = value;
			});
		},

    hide: function(){
		return this[0].style.display = 'none';
    },

    show: function(){
		return this[0].style.display = '';
    },

		// TODO: test on browsers that don't support dataset
		data: function (key, value) {
			if ( value === undefined ) {
				return this[0].dataset ? this[0].dataset[key] : this[0].getAttribute('data-' + key);
			}

			return this.each(function () {
				this.dataset ? (this.dataset[key] = value) : this.setAttribute('data-' + key, value);
			});
		},
/*
		tap: function (fn) {
			return this.each(function () {
				this._tapInstance = new $.Tap(this);
				$(this).bind('tap', fn);
			});
		},
*/
		find: function (q) {
			return $(q, this[0]);
		},

		translate: function (x, y) {
			if ( x === undefined ) {
				if ( !$.transform ) {
					return {
						x: this.style('left'),
						y: this.style('top')
					};
				}

				var matrix = w.getComputedStyle(this[0], null)[$.transform];

				// TODO: Extend to other CSSMatrix vendors
				if ( w.WebKitCSSMatrix ) {
					matrix = new w.WebKitCSSMatrix(matrix);
					return {
						x: matrix.e,
						y: matrix.f
					};
				}

				matrix = matrix.split('(')[1].replace(/[^\d\-.,]/g, '').split(',');
				return {
					x: +matrix[4],
					y: +matrix[5]
				};
			}

			return this.each(function () {
				if ( $.transform ) {
					this.style[$.transform] = 'translate3d(' + x + 'px,' + y + 'px,0)'; // 'translate(' + x + 'px,' + y + 'px)' + ($.has3d ? ' translateZ(0)' : '');
				} else {
					this.style.top = y + 'px';
					this.style.left = x + 'px';
				}
			});
		},

		transition: function (values, onCompletion) {
			return this.each(function () {
				transition(this, values, onCompletion);
			});
		},

		refresh: function () {
			return this.each(function () {
				this.offsetHeight;
			});
		}
	};

	$.iphoneDetect = function(ua)
		{
		var isiPhone = /iPhone/i.test(ua);
		return isiPhone;
		};

	$.create = function (tag, parms) {
		if ( typeof tag == 'object' ) {
			parms = tag;
			tag = parms.tag;
			delete parms.tag;
		}

		var el = doc.createElement(tag),
			i;

		parms = parms || {};

		for ( i in parms ) {
			el[i] = parms[i];
		}

		return el;
	};

	$.escape = function (value) {
		return encodeURIComponent(value);
	};

	$.ajax = function (url, parms) {
		parms = parms || {};
		var req = new XMLHttpRequest(),
			post = parms.post || null,
			callback = parms.callback || null,
			timeout = parms.timeout || null;

		req.onreadystatechange = function () {
			if ( req.readyState != 4 ) return;

			// Error
			if ( req.status != 200 && req.status != 304 ) {
				if ( callback ) callback(false);
				return;
			}

			var response = req.getResponseHeader('Content-Type') == 'application/json' ? w.JSON.parse(req.responseText) : req.responseText;

			if ( callback ) callback(response);
		};

		if ( post ) {
			req.open('POST', url, true);
			req.setRequestHeader('Content-type', 'application/x-www-form-urlencoded');
		} else {
			req.open('GET', url, true);
		}

		req.setRequestHeader('X-Requested-With', 'XMLHttpRequest');

		req.send(post);

		if ( timeout ) {
			setTimeout(function () {
				req.onreadystatechange = function () {};
				req.abort();
				if ( callback ) callback(false);
			}, timeout);
		}
	};

	// Better random number generator
	var randomGenerator = Alea();
	$.seedRandom = function (seed) {
		randomGenerator = Alea();
	};
	$.random = function (min, max, count) {
		if (count !== undefined)
			{
			var generated = [],
				newNumber;
			while (generated.length < count)
				{
				newNumber = (randomGenerator() * (max - min + 1) >>> 0) + min;
				if (generated.indexOf(newNumber) === -1)
					{
					generated.push(newNumber);
					}
				}
			return generated;
			}

		if ( min instanceof Array ) {
			max = min.length;
			min = 0;
		} else if ( max === undefined ) {
			max = min;
			min = 0;
		}
		return (randomGenerator() * (max - min + 1) >>> 0) + min;
	};

	$.vendor = (function () {
		var vendors = 't,webkitT,MozT,msT,OT'.split(','),
			transform,
			i = 0,
			l = vendors.length,
			el = $.create('div');

		for ( ; i < l; i++ ) {
			transform = vendors[i] + 'ransform';
			if ( transform in el.style ) {
				return vendors[i].substr(0, vendors[i].length - 1);
			}
		}

		return false;
	})();

	$.hasTouch = 'ontouchstart' in w;
	$.eventStart = $.hasTouch ? 'touchstart' : 'mousedown';
	$.eventMove = $.hasTouch ? 'touchmove' : 'mousemove';
	$.eventEnd = $.hasTouch ? 'touchend' : 'mouseup';
	$.eventCancel = $.hasTouch ? 'touchcancel' : 'mousecancel';
	$.eventTransitionEnd = (function () {
		if ( $.vendor === false ) return false;

		var transitionEnd = {
				''			: 'transitionend',
				'webkit'	: 'webkitTransitionEnd',
				'Moz'		: 'transitionend',
				'O'			: 'oTransitionEnd',
				'ms'		: 'MSTransitionEnd'
			};

		return transitionEnd[$.vendor];
	})();

	$.transform = prefixStyle('transform');
	$.has3d = prefixStyle('perspective') in $.create('div').style;
	$.hasTransition = prefixStyle('transition') in $.create('div').style;

	$.EASE = {
		LINEAR: '0.250, 0.250, 0.750, 0.750',
		IN: '0.420, 0.000, 1.000, 1.000',
		OUT: '0.000, 0.000, 0.580, 1.000',
		INOUT: '0.420, 0.000, 0.580, 1.000',
		CUBIC: { IN: '0.550, 0.055, 0.675, 0.190', OUT: '0.215, 0.610, 0.355, 1.000', INOUT: '0.645, 0.045, 0.355, 1.000' },
		SINE: { IN: '0.470, 0.000, 0.745, 0.715', OUT: '0.390, 0.575, 0.565, 1.000', INOUT: '0.445, 0.050, 0.550, 0.950' }
	};

	return $;
})(this, document);