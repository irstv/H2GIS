---
layout: docs
title: ST_MPointFromText
category: h2spatial/geometry-conversion
description: Well Known Text &rarr; <code>MULTIPOINT</code>
prev_section: ST_MLineFromText
next_section: ST_MPolyFromText
permalink: /docs/dev/ST_MPointFromText/
---

### Signature

{% highlight mysql %}
GEOMETRY ST_MPointFromText(varchar WKT, int srid);
{% endhighlight %}

### Description

Converts a Well Known Text `WKT` String into a `MULTIPOINT`.

{% include sfs-1-2-1.html %}

### Example

{% highlight mysql %}
SELECT ST_MPointFromText(
    'MULTIPOINT(5 5, 1 2, 3 4, 20 3)',2154);
-- Answer: MULTIPOINT((5 5), (1 2), (3 4), (20 3))
{% endhighlight %}

##### See also

* <a href="https://github.com/irstv/H2GIS/blob/master/h2spatial/src/main/java/org/h2gis/h2spatial/internal/function/spatial/convert/ST_MPointFromText.java" target="_blank">Source code</a>
