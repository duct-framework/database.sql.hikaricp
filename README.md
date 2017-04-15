# Duct database.sql.hikaricp

[Integrant][] methods for connecting to a SQL database from the
[Duct][] framework using [HikariCP][], an efficient connection pool.

[integrant]: https://github.com/weavejester/integrant
[duct]:      https://github.com/duct-framework/duct
[hikaricp]:  https://github.com/brettwooldridge/HikariCP

## Installation

To install, add the following to your project `:dependencies`:

    [duct/database.sql.hikaricp "0.1.0-SNAPSHOT"]

## Usage

This library depends on [database.sql][] and provides the
`:duct.database.sql/hikaricp` key, which is derived from
`:duct.database/sql`.

The key takes the same [config options][] as the Clojure [hikari-cp][]
wrapper library, and returns a `duct.database.sql.Boundary` record
that contains a database spec.

```edn
{:duct.database.sql/hikaricp {:jdbc-url "jdbc:sqlite:db/example.sqlite"}}
```

[database.sql]:   https://github.com/duct-framework/database.sql
[config options]: https://github.com/tomekw/hikari-cp#configuration-options
[hikari-cp]:      https://github.com/tomekw/hikari-cp

## License

Copyright © 2017 James Reeves

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
