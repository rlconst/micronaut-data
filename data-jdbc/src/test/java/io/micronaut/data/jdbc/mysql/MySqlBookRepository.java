/*
 * Copyright 2017-2020 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.data.jdbc.mysql;

import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.tck.entities.Book;
import io.micronaut.data.tck.repositories.BookRepository;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@JdbcRepository(dialect = Dialect.MYSQL)
public abstract class MySqlBookRepository extends BookRepository {
    public MySqlBookRepository(MySqlAuthorRepository authorRepository) {
        super(authorRepository);
    }

    @Transactional
    abstract List<Book> findFirst500ByLastUpdatedBeforeForUpdate(LocalDateTime lastUpdated);
}
