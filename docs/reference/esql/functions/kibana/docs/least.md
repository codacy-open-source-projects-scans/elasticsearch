<!--
This is generated by ESQL's AbstractFunctionTestCase. Do no edit it. See ../README.md for how to regenerate it.
-->

### LEAST
Returns the minimum value from multiple columns. This is similar to <<esql-mv_min>> except it is intended to run on multiple columns at once.

```
ROW a = 10, b = 20
| EVAL l = LEAST(a, b)
```