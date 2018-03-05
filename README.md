# OSM2Graph

Osmosis plugin to generate graph files from OSM files.

# Usage

You need to have [osmosis](https://wiki.openstreetmap.org/wiki/Osmosis) and put the `osm2graph.jar` file in the `plugins/` 
directory of `osmosis`.

```bash
bin/osmosis --rb input_map.osm.pbf \ 
            --tf reject-relations \
            --tf accept-ways highway=$(cat resources/highway-filter.cmd) natural=coastline junction=roundabout \
            --used-node \
            --osm2graph file=output_map.mapgr writer=insa2018 id=MAP-ID name="A map name"
```

The only mandatory parameter is `file`, the default `writer` is `insa2018`, the next parameter (`id` and `name`) are only 
mandatory if required by the specified writer (e.g. for `insa2016`, `id` must be convertible to `int` for `insa2016` and `name`
should not be specified).

You can use any input mode for `osmosis` (pbf, xml, mysql, ...). The `--tf reject-relations` and `--used-node` options
are optional but can speed up the process quite a bit.

The `--tf accept-ways` option is **mandatory** because OSM2Graph does not handle all ways:

- The `natural=coastline` parameter is optional, you can use it to add coastline to your graph (useful for quick drawings).
- The `highway=...` parameter is **mandatory** &mdash; The list of supported values can be found in `resources/highway-filter.cmd`, 
you can use a subset of this if you want.

# Using a custom writer

You can create a custom writer by implementing the `GraphWriter` interface and then add it to the `GraphWriterFactory` class:

```java
static {
    ALLOWED_WRITERS.put("insa2016", BinaryGraphWriterInsa2016.class);
    ALLOWED_WRITERS.put("insa2018", BinaryGraphWriterInsa2018.class);
    ALLOWED_WRITERS.put("my-custom-writer", MyCustomWriter.class);
}
```

You writer should be default constructible and implement the following methods:

- `writeGraph` - the actual implementation of the write.
- `setOutputStream` - method used to set the output stream, the stream given by the task is a `BufferedInputStream`
from the file in the parameters.
- `getDefaultExtension` - used to add an extension when the user did not specify one.
- `validate` - validate the set of parameters given by the user and throw exceptions if something is wrong.
