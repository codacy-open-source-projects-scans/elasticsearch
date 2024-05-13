<!--
This is generated by ESQL's AbstractFunctionTestCase. Do no edit it. See ../README.md for how to regenerate it.
-->

### LOG10
Returns the logarithm of a value to base 10. The input can be any numeric value, the return value is always a double.

Logs of 0 and negative numbers return `null` as well as a warning.

```
ROW d = 1000.0 
| EVAL s = LOG10(d)
```