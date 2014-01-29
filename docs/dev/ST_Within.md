---
layout: docs
title: ST_Within
category: h2spatial/predicates
description: Return true if Geometry A is within Geometry B
prev_section: ST_Touches
next_section: h2spatial/projections
permalink: /docs/dev/ST_Within/
---

### Signatures

{% highlight mysql %}
boolean ST_Within(GEOMETRY geomA, GEOMETRY geomB);
{% endhighlight %}

### Description

Returns true if `geomA` is within `geomB`.

Within means that `geomA` is completely inside `geomB`. Every point of `geomA` is a point of `geomB`, and the interiors of the two geometries have at least one point in common.

As a consequence, if `ST_Within(geomA, geomB)` is TRUE and `ST_Within(geomB, geomA)` is TRUE, `geomA` and `geomB` are considered to be equal (in the sense of `ST_Equals`).

{% include sfs-1-2-1.html %}

##### Remark
   * `GEOMETRYCOLLECTION`s are not taken into account,
   * Within is the inverse of contains (See `ST_Contains`).

### Examples

##### Cases where `ST_Within` is true
 
{% highlight mysql %}
SELECT ST_Within(geomA, geomB) FROM input_table;
-- Answer:    True
{% endhighlight %}

| geomA POLYGON | geomB POLYGON |
| ----|---- |
| POLYGON ((2 2, 7 2, 7 5, 2 5, 2 2)) | POLYGON ((1 1, 8 1, 8 7, 1 7, 1 1)) |

<img class="displayed" src="../ST_Within_1.png"/>

| geomA LINESTRING | geomB POLYGON |
| ----|---- |
| LINESTRING (2 6, 6 2) | POLYGON ((1 1, 8 1, 8 7, 1 7, 1 1)) |

<img class="displayed" src="../ST_Within_2.png"/>

| geomA POINT | geomB POLYGON |
| ----|---- |
| POINT (4 4) | POLYGON ((1 1, 8 1, 8 7, 1 7, 1 1)) |

<img class="displayed" src="../ST_Within_3.png"/>

| geomA POLYGON | geomB POLYGON |
| ----|---- |
| POLYGON ((1 2, 6 2, 6 5, 1 5, 1 2)) | POLYGON ((1 1, 8 1, 8 7, 1 7, 1 1)) |

<img class="displayed" src="../ST_Within_4.png"/>

| geomA LINESTRING | geomB POLYGON |
| ----|---- |
| LINESTRING (1 2, 1 6, 5 2) | POLYGON ((1 1, 8 1, 8 7, 1 7, 1 1)) |

<img class="displayed" src="../ST_Within_5.png"/>

| geomA LINESTRING | geomB LINESTRING |
| ----|---- |
| LINESTRING (3 5, 5 3) | LINESTRING (2 1, 5 3, 2 6) |

<img class="displayed" src="../ST_Within_6.png"/>

| geomA POINT | geomB LINESTRING |
| ----|---- |
| POINT (4 4) | LINESTRING (2 1, 5 3, 2 6) |

<img class="displayed" src="../ST_Within_7.png"/>

##### Cases where `ST_Within` is false
 
{% highlight mysql %}
SELECT ST_Within(geomA, geomB) FROM input_table;
-- Answer:    False
{% endhighlight %}

| geomA POLYGON | geomB POLYGON |
| ----|---- |
| POLYGON ((0 2, 5 2, 5 5, 0 5, 0 2)) | POLYGON ((1 1, 8 1, 8 7, 1 7, 1 1)) |

<img class="displayed" src="../ST_Within_8.png"/>

| geomA LINESTRING | geomB POLYGON |
| ----|---- |
| LINESTRING (2 6, 0 8) | POLYGON ((1 1, 8 1, 8 7, 1 7, 1 1)) |

<img class="displayed" src="../ST_Within_9.png"/>

| geomA POINT | geomB POLYGON |
| ----|---- |
| POINT (8 4) | POLYGON ((1 1, 8 1, 8 7, 1 7, 1 1)) |

<img class="displayed" src="../ST_Within_10.png"/>

| geomA POINT | geomB POLYGON |
| ----|---- |
| POINT (8 4) | POLYGON ((1 1, 7 1, 7 7, 1 7, 1 1)) |

<img class="displayed" src="../ST_Within_11.png"/>

##### See also

* [`ST_Contains`](../ST_Contains), [`ST_Overlaps`](../ST_Overlaps), [`ST_Touches`](../ST_Touches)
* <a href="https://github.com/irstv/H2GIS/blob/master/h2spatial/src/main/java/org/h2gis/h2spatial/internal/function/spatial/predicates/ST_Within.java" target="_blank">Source code</a>
