(function ()
    {

    function printObject(o)
        {
        alert(objectToString(out));
        }

    function objectToString(o)
        {
        var out = '';
        for (var p in o)
            {
            out += p + ': ' + o[p] + '\n';
            }
        return out;
        }

    // Remove all children from a specified element
    function removeAllChildren(el)
        {
        while (el.hasChildNodes())
            {
            el.removeChild(el.lastChild);
            }
        }

    // Recursively add a property
    function copyProperty(prop, from, to)
        {
        if (!from.hasOwnProperty(prop))
            {
            return true;
            }

        if (Array.isArray(from[prop]))
            {
            return false;
            }

        var alreadyExists = true;
        // if the cache does not contain a specific property, add it w/ default value
        if (!to.hasOwnProperty(prop))
            {
            to[prop] = {};
            alreadyExists = false;
            }

        var found = false;
        for (var subprop in from[prop])
            {
            found = copyProperty(subprop, from[prop], to[prop]);
            }

        if (!found && !alreadyExists)
            {
            to[prop] = from[prop];
            }
        }

    function isEmptyObj(obj)
        {
        return obj === null || obj === undefined || Object.keys(obj).length === 0;
        }

    function intOrZero(value)
        {
        return value === null || value === undefined || isNaN(parseFloat(value)) ? 0 : parseInt(value, 10);
        }

    // Clonses only properties, not methods
    function cloneStructure(structure)
        {
        return JSON.parse(JSON.stringify(structure));
        }

    window.printObject = printObject;
    window.objectToString = objectToString;
    window.removeAllChildren = removeAllChildren;
    window.copyProperty = copyProperty;
    window.isEmptyObj = isEmptyObj;
    window.intOrZero = intOrZero;
    window.cloneStructure = cloneStructure;

    })();