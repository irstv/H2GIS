---
layout: docs
title: ST_ToMultiLine
category: h2spatial-ext/geometry-conversion
description: Construct a <code>MULTILINESTRING</code> from a Geometry's coordinates
prev_section: ST_Holes
next_section: ST_ToMultiPoint
permalink: /docs/dev/ST_ToMultiLine/
---

### Signature

{% highlight mysql %}
MULTILINESTRING ST_ToMultiLine(GEOMETRY geom);
{% endhighlight %}

### Description

Constructs a `MULTILINESTRING` from the given Geometry's coordinates. Returns
`MULTILINESTRING EMPTY` for Geometries of dimension 0.

### Examples

{% highlight mysql %}
SELECT ST_ToMultiLine('POLYGON ((0 0, 10 0, 10 6, 0 6, 0 0), 
                                (1 1, 2 1, 2 5, 1 5, 1 1))');
-- Answer: MULTILINESTRING ((0 0, 10 0, 10 5, 0 5, 0 0), 
--                          (1 1, 2 1, 2 4, 1 4, 1 1))
{% endhighlight %}

<img class="displayed" src="../ST_ToMultiLine1.png"/>

{% highlight mysql %}
SELECT ST_ToMultiLine('GEOMETRYCOLLECTION(
   LINESTRING(1 4 3, 10 7 9, 12 9 22), 
   POLYGON((1 1 -1, 3 1 0, 3 2 1, 1 2 2, 1 1 -1)))');
-- Answer: MULTILINESTRING ((1 4, 10 7, 12 9),
--                          (1 1, 3 1, 3 2, 1 2, 1 1))
{% endhighlight %}

<img class="displayed" src="../ST_ToMultiLine2.png"/>

##### Stupid cases

{% highlight mysql %}
SELECT ST_ToMultiLine('POINT(2 4)');
-- Answer: MULTILINESTRING EMPTY
{% endhighlight %}

##### See also

* <a href="https://github.com/irstv/H2GIS/blob/master/h2spatial-ext/src/main/java/org/h2gis/h2spatialext/function/spatial/convert/ST_ToMultiLine.java" target="_blank">Source code</a>
