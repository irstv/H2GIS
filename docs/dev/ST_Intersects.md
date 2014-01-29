---
layout: docs
title: ST_Intersects
category: h2spatial/predicates
description: Return true if Geometry A intersects Geometry B
prev_section: ST_Equals
next_section: ST_Overlaps
permalink: /docs/dev/ST_Intersects/
---

### Signatures

{% highlight mysql %}
boolean ST_Intersects(GEOMETRY geomA, GEOMETRY geomB);
{% endhighlight %}

### Description

Returns true if `geomA` intersects `geomB`.

Intersects means that `geomA` and `geomB` have at least one point in common.

{% include sfs-1-2-1.html %}

##### Remark
   * `GEOMETRYCOLLECTION`s are not taken into account.

### Examples

##### Cases where `ST_Intersects` is true
 
{% highlight mysql %}
SELECT ST_Intersects(geomA, geomB) FROM input_table;
-- Answer:    True
{% endhighlight %}

| geomA POLYGON | geomB POLYGON |
| ----|---- |
| POLYGON ((1 1, 4 1, 4 5, 1 5, 1 1)) | POLYGON ((3 2, 6 2, 6 6, 3 6, 3 2)) |

<img class="displayed" src="../ST_Intersects_1.png"/>

| geomA POLYGON | geomB LINESTRING |
| ----|---- |
| POLYGON ((1 1, 4 1, 4 5, 1 5, 1 1)) | LINESTRING (2 4, 7 4) |

<img class="displayed" src="../ST_Intersects_2.png"/>

| geomA POLYGON | geomB POINT |
| ----|---- |
| POLYGON ((1 1, 4 1, 4 5, 1 5, 1 1)) | POINT (3 3) |

<img class="displayed" src="../ST_Intersects_3.png"/>

| geomA LINESTRING | geomB LINESTRING |
| ----|---- |
| LINESTRING (2 1, 5 3, 2 6) | LINESTRING (1 3, 4 6) |

<img class="displayed" src="../ST_Intersects_4.png"/>

| geomA LINESTRING | geomB POINT |
| ----|---- |
| LINESTRING (2 1, 5 3, 2 6) | POINT (2 6) |

<img class="displayed" src="../ST_Intersects_5.png"/>

| geomA POLYGON | geomB MULTIPOLYGON |
| ----|---- |
| POLYGON ((1 1, 4 1, 4 5, 1 5, 1 1)) | MULTIPOLYGON (((4 2, 7 2, 7 6, 4 6, 4 2)), ((0 6, 1 6, 1 7, 0 7, 0 6))) |

<img class="displayed" src="../ST_Intersects_6.png"/>

| geomA POLYGON | geomB MULTILINESTRING |
| ----|---- |
| POLYGON ((1 1, 4 1, 4 5, 1 5, 1 1)) | MULTILINESTRING ((2 5, 7 5), (6 1, 6 4)) |

<img class="displayed" src="../ST_Intersects_7.png"/>

| geomA POLYGON | geomB MULTIPOINT |
| ----|---- |
| POLYGON ((1 1, 4 1, 4 5, 1 5, 1 1)) | MULTIPOINT ((4 3), (6 2)) |

<img class="displayed" src="../ST_Intersects_8.png"/>

##### Cases where `ST_Intersects` is false
 
{% highlight mysql %}
SELECT ST_Intersects(geomA, geomB) FROM input_table;
-- Answer:    False
{% endhighlight %}

| geomA POLYGON | geomB POLYGON |
| ----|---- |
| POLYGON ((1 1, 4 1, 4 5, 1 5, 1 1)) | POLYGON ((6 3, 7 3, 7 6, 6 6, 6 3)) |

<img class="displayed" src="../ST_Intersects_9.png"/>

##### See also

* [`ST_Intersection`](../ST_Intersection), [`ST_Overlaps`](../ST_Overlaps), [`ST_Contains`](../ST_Contains), [`ST_Touches`](../ST_Touches)
* <a href="https://github.com/irstv/H2GIS/blob/master/h2spatial/src/main/java/org/h2gis/h2spatial/internal/function/spatial/predicates/ST_Intersects.java" target="_blank">Source code</a>
