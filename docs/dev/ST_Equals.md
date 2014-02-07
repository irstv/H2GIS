---
layout: docs
title: ST_Equals
category: h2spatial/predicates
description: Return true if Geometry A equals Geometry B
prev_section: ST_Disjoint
next_section: ST_Intersects
permalink: /docs/dev/ST_Equals/
---

### Signatures

{% highlight mysql %}
boolean ST_Equals(GEOMETRY geomA, GEOMETRY geomB);
{% endhighlight %}

### Description

Return true if `geomA` is topologically equal to `geomB`.

Equal means:
  * Shapes of `geomA` and `geomB` have exactly the same shape. I.e., `ST_Within(A,B) = true` and `ST_Within(B,A) = true`.
  * The directionality and the order of points is ignored.

{% include sfs-1-2-1.html %}

### Examples

##### Cases where `ST_Equals` is true
 
{% highlight mysql %}
SELECT ST_Equals(geomA, geomB) FROM input_table;
-- Answer:    True
{% endhighlight %}

| geomA POLYGON | geomB POLYGON |
| ----|---- |
| POLYGON ((1 1, 4 1, 4 4, 1 4, 1 1)) | POLYGON ((1 1, 4 1, 4 4, 1 4, 1 1)) |

<img class="displayed" src="../ST_Equals_1.png"/>

| geomA POLYGON | geomB POLYGON |
| ----|---- |
| POLYGON ((1 1, 4 1, 4 4, 1 4, 1 1)) | POLYGON ((4 4, 4 1, 1 1, 1 4, 4 4)) |

<img class="displayed" src="../ST_Equals_2.png"/>

| geomA LINESTRING | geomB LINESTRING |
| ----|---- |
| LINESTRING (1 1, 4 4) | LINESTRING (1 1, 3 3, 4 4) |

<img class="displayed" src="../ST_Equals_3.png"/>

##### See also

* [`ST_Relate`](../ST_Relate), [`ST_Within`](../ST_Within)
* <a href="https://github.com/irstv/H2GIS/blob/master/h2spatial/src/main/java/org/h2gis/h2spatial/internal/function/spatial/predicates/ST_Equals.java" target="_blank">Source code</a>
