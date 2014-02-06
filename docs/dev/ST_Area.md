---
layout: docs
title: ST_Area
category: h2spatial/properties
description: Compute Geometry area
prev_section: h2spatial/properties
next_section: ST_Boundary
permalink: /docs/dev/ST_Area/
---

### Signature

{% highlight mysql %}
double ST_Area(GEOMETRY geom);
{% endhighlight %}

### Description

Computes `GEOMETRY` area. If a `GEOMETRY` has no area (like `POINT` or `LINESTRING`) return 0.
The compute area for `GEOMETRYCOLLECTION` is add to each Geometry area.

{% include sfs-1-2-1.html %}

### Examples

{% highlight mysql %}
SELECT ST_Area('POINT(0 12)');
SELECT ST_Area('LINESTRING(5 4, 1 1, 3 4, 4 5)');
-- Answer: 0.0
--         0.0

SELECT ST_Area('POLYGON((0 0, 10 0, 10 10, 0 10, 0 0))');
-- Answer: 100.0

SELECT ST_Area('MULTIPOLYGON(((0 0, 10 0, 10 10, 0 10, 0 0), 
                (5 4, 1 1, 3 4, 4 5, 5 4)))');
-- Answer: 96.0

SELECT ST_Area('GEOMETRYCOLLECTION(
                LINESTRING(5 4, 1 1, 3 4, 4 5), 
                POINT(0 12), 
                POLYGON((0 0, 10 0, 10 10, 0 10, 0 0)), 
                POLYGON((5 4, 1 1, 3 4, 4 5, 5 4)))');
-- Answer: 104.0
{% endhighlight %}

##### See also

* <a href="https://github.com/irstv/H2GIS/blob/master/h2spatial/src/main/java/org/h2gis/h2spatial/internal/function/spatial/properties/ST_Area.java" target="_blank">Source code</a>
