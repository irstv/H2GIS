---
layout: docs
title: ST_GeometryType
category: h2spatial/properties
description: Return the type of Geometry
prev_section: ST_GeometryN
next_section: ST_InteriorRingN
permalink: /docs/dev/ST_GeometryType/
---

### Signature

{% highlight mysql %}
varchar ST_GeometryType(GEOMETRY geom);
{% endhighlight %}

### Description

Returns the type of Geometry : `POINT`, `LINESTRING`, `POLYGON`...

{% include sfs-1-2-1.html %}

### Examples

{% highlight mysql %}
SELECT ST_GeometryType('MULTIPOINT((4 4), (1 1), (1 0), (0 3)))');
-- Answer: MultiPoint

SELECT ST_GeometryType('LINESTRING(2 1, 1 3, 5 2)');
-- Answer: LineString

SELECT ST_GeometryType('MULTIPOLYGON(((0 2, 3 2, 3 6, 0 6, 0 2)), 
                    ((5 0, 7 0, 7 1, 5 1, 5 0)))');
-- Answer: MultiPolygon

SELECT ST_GeometryType('GEOMETRYCOLLECTION(
                      MULTIPOINT((4 4), (1 1), (1 0), (0 3)), 
                      LINESTRING(2 6, 6 2), 
                      POLYGON((1 2, 4 2, 4 6, 1 6, 1 2)))');
-- Answer: GeometryCollection
{% endhighlight %}

##### See also

* [`ST_Dimension`](../ST_Dimension)
* <a href="https://github.com/irstv/H2GIS/blob/master/h2spatial/src/main/java/org/h2gis/h2spatial/internal/function/spatial/properties/ST_GeometryType.java" target="_blank">Source code</a>
