---
layout: docs
title: ST_Extent
category: h2spatial-ext/properties
description: Return the minimum bounding box of a <code>GEOMETRYCOLLECTION</code>
prev_section: ST_Explode
next_section: ST_XMax
permalink: /docs/dev/ST_Extent/
---
 
### Signature

{% highlight mysql %}
GEOMETRY ST_Extent(GEOMETRY geom);
GEOMETRY ST_Extent(GEOMETRYCOLLECTION geom);
{% endhighlight %}

### Description

Returns the minimum bounding box that encloses `geom` as a `GEOMETRY`.

### Examples

{% highlight mysql %}
SELECT ST_Extent('MULTIPOINT((5 6), (1 2), (3 4), (10 3))'::Geometry);
-- Answer: POLYGON ((1 2, 1 6, 10 6, 10 2, 1 2))
{% endhighlight %}

<img class="displayed" src="../ST_Extent1.png"/>

{% highlight mysql %}
SELECT ST_Extent('POINT(5 6)'::Geometry);
-- Answer: POINT(5 6)
{% endhighlight %}

##### Comparison with [`ST_Envelope`](../ST_Envelope)

{% include extent-envelope-cf.html %}

##### See also
* [`ST_Envelope`](../ST_Envelope)
* <a href="https://github.com/irstv/H2GIS/blob/master/h2spatial-ext/src/main/java/org/h2gis/h2spatialext/function/spatial/properties/ST_Extent.java" target="_blank">Source code</a>
