(function(w) {

    var lastTap = {};

    GAME.game.onTouchEvent = function(e)
        {console.log('android touch event');
        var timeStamp = new Date().getTime();
        if (GAME.game.isTouchDisabled()
            || (timeStamp - lastTap.time <= 300 && lastTap.element !== e.target)
            || (lastTap.top == e.target.offsetTop
                && lastTap.left == e.target.offsetLeft
                && timeStamp - lastTap.time <= 300
                && lastTap.element === e.target))
            {
            lastTap.element = e.target;
            return;
            }

        lastTap.element = e.target;
        lastTap.top = e.target.offsetTop;
        lastTap.left = e.target.offsetLeft;
        lastTap.time = timeStamp;
        GAME.game.routeTouchEvent(e);
        };

    function onLoaded()
        {console.log('android onloaded');
        lastTap.element = '';
        lastTap.top = -1;
        lastTap.left = -1;
        lastTap.time = new Date().getTime();
        }

    $(w).bind('load', onLoaded);

})(this);