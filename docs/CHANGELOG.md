## Changelog for v2.2.4

- Add ST_EnvelopeAsText function
- Add ST_AsOverpassBbox function
- Add ST_OverpassDownloader function
- Fix bug when read GeometryCollection with the ST_GeomFromGeoJSON function 
- Fix github actions
- Fix mixed srid error on empty geometry with ST_Extent #1400
- Remove transitive dependency from flatgeobuffer to JTS version 1.19 (should use 1.20 of jts-core)
  
