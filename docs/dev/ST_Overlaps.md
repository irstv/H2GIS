---
layout: docs
title: ST_Overlaps
category: h2spatial/predicates
description: Return true if Geometry A overlaps Geometry B
prev_section: ST_Intersects
next_section: ST_Relate
permalink: /docs/dev/ST_Overlaps/
---

### Signatures

{% highlight mysql %}
boolean ST_Overlaps(GEOMETRY geomA, GEOMETRY geomB);
{% endhighlight %}

### Description

Returns true if `geomA` overlaps `geomB`.

Overlaps means that:
  * `geomA` and `geomB` have some but not all points in common, 
  * `geomA` and `geomB` have the same dimension, 
  * The intersection of the interiors of `geomA` and `geomB` has the same dimension as the geometries themselves.

{% include sfs-1-2-1.html %}

##### Remark
   * `GEOMETRYCOLLECTION`s are not taken into account.

### Examples

##### Cases where `ST_Overlaps` is true
 
{% highlight mysql %}
SELECT ST_Overlaps(geomA, geomB) FROM input_table;
-- Answer:    True
{% endhighlight %}

| geomA POLYGON | geomB POLYGON |
| ----|---- |
| POLYGON ((1 1, 4 1, 4 5, 1 5, 1 1)) | POLYGON ((3 2, 6 2, 6 6, 3 6, 3 2)) |

<img class="displayed" src="../ST_Overlaps_1.png"/>

| geomA LINESTRING | geomB LINESTRING |
| ----|---- |
| LINESTRING (2 1, 5 3, 2 6) | LINESTRING (3 5, 4 4, 6 7) |

<img class="displayed" src="../ST_Overlaps_2.png"/>

| geomA MULTIPOINT | geomB MULTIPOINT |
| ----|---- |
| MULTIPOINT ((5 1), (3 3), (2 5), (4 5)) | MULTIPOINT ((3 3), (5 4), (2 6)) |

<img class="displayed" src="../ST_Overlaps_3.png"/>

| geomA POLYGON | geomB MULTIPOLYGON |
| ----|---- |
| POLYGON ((1 1, 4 1, 4 5, 1 5, 1 1)) | MULTIPOLYGON (((3 2, 6 2, 6 6, 3 6, 3 2)), ((0 6, 1 6, 1 7, 0 7, 0 6))) |

<img class="displayed" src="../ST_Overlaps_4.png"/>

##### Cases where `ST_Overlaps` is false
 
{% highlight mysql %}
SELECT ST_Overlaps(geomA, geomB) FROM input_table;
-- Answer:    False
{% endhighlight %}

| geomA POLYGON | geomB POLYGON |
| ----|---- |
| POLYGON ((1 1, 4 1, 4 5, 1 5, 1 1)) | POLYGON ((4 5, 7 5, 7 6, 4 6, 4 5)) |

<img class="displayed" src="../ST_Overlaps_5.png"/>

| geomA LINESTRING | geomB LINESTRING |
| ----|---- |
| LINESTRING (2 1, 5 3, 2 6) | LINESTRING (1 3, 4 6) |

<img class="displayed" src="../ST_Overlaps_6.png"/>

##### See also

* [`ST_Intersects`](../ST_Intersects), [`ST_Contains`](../ST_Contains)
* <a href="https://github.com/irstv/H2GIS/blob/master/h2spatial/src/main/java/org/h2gis/h2spatial/internal/function/spatial/predicates/ST_Overlaps.java" target="_blank">Source code</a>
