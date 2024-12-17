/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the "Elastic License
 * 2.0", the "GNU Affero General Public License v3.0 only", and the "Server Side
 * Public License v 1"; you may not use this file except in compliance with, at
 * your election, the "Elastic License 2.0", the "GNU Affero General Public
 * License v3.0 only", or the "Server Side Public License, v 1".
 */

package org.elasticsearch.lucene;

import org.elasticsearch.client.Request;
import org.elasticsearch.cluster.metadata.IndexMetadata;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.IndexSettings;
import org.elasticsearch.repositories.fs.FsRepository;
import org.elasticsearch.test.cluster.util.Version;

import java.util.stream.IntStream;

import static org.elasticsearch.test.rest.ObjectPath.createFromResponse;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

public class LuceneCompatibilityIT extends AbstractLuceneIndexCompatibilityTestCase {

    static {
        clusterConfig = config -> config.setting("xpack.license.self_generated.type", "trial");
    }

    public LuceneCompatibilityIT(Version version) {
        super(version);
    }

    public void testRestoreIndex() throws Exception {
        final String repository = suffix("repository");
        final String snapshot = suffix("snapshot");
        final String index = suffix("index");
        final int numDocs = 1234;

        logger.debug("--> registering repository [{}]", repository);
        registerRepository(
            client(),
            repository,
            FsRepository.TYPE,
            true,
            Settings.builder().put("location", REPOSITORY_PATH.getRoot().getPath()).build()
        );

        if (VERSION_MINUS_2.equals(clusterVersion())) {
            logger.debug("--> creating index [{}]", index);
            createIndex(
                client(),
                index,
                Settings.builder()
                    .put(IndexMetadata.SETTING_NUMBER_OF_SHARDS, 1)
                    .put(IndexMetadata.SETTING_NUMBER_OF_REPLICAS, 0)
                    .put(IndexSettings.INDEX_SOFT_DELETES_SETTING.getKey(), true)
                    .build()
            );

            logger.debug("--> indexing [{}] docs in [{}]", numDocs, index);
            final var bulks = new StringBuilder();
            IntStream.range(0, numDocs).forEach(n -> bulks.append(Strings.format("""
                {"index":{"_id":"%s","_index":"%s"}}
                {"test":"test"}
                """, n, index)));

            var bulkRequest = new Request("POST", "/_bulk");
            bulkRequest.setJsonEntity(bulks.toString());
            var bulkResponse = client().performRequest(bulkRequest);
            assertOK(bulkResponse);
            assertThat(entityAsMap(bulkResponse).get("errors"), allOf(notNullValue(), is(false)));

            logger.debug("--> creating snapshot [{}]", snapshot);
            createSnapshot(client(), repository, snapshot, true);
            return;
        }

        if (VERSION_MINUS_1.equals(clusterVersion())) {
            ensureGreen(index);

            assertThat(indexLuceneVersion(index), equalTo(VERSION_MINUS_2));
            assertDocCount(client(), index, numDocs);

            logger.debug("--> deleting index [{}]", index);
            deleteIndex(index);
            return;
        }

        if (VERSION_CURRENT.equals(clusterVersion())) {
            var restoredIndex = suffix("index-restored");
            logger.debug("--> restoring index [{}] as archive [{}]", index, restoredIndex);

            // Restoring the archive will fail as Elasticsearch does not support reading N-2 yet
            var request = new Request("POST", "/_snapshot/" + repository + "/" + snapshot + "/_restore");
            request.addParameter("wait_for_completion", "true");
            request.setJsonEntity(Strings.format("""
                {
                  "indices": "%s",
                  "include_global_state": false,
                  "rename_pattern": "(.+)",
                  "rename_replacement": "%s",
                  "include_aliases": false
                }""", index, restoredIndex));
            var responseBody = createFromResponse(client().performRequest(request));
            assertThat(responseBody.evaluate("snapshot.shards.total"), equalTo((int) responseBody.evaluate("snapshot.shards.failed")));
            assertThat(responseBody.evaluate("snapshot.shards.successful"), equalTo(0));
        }
    }
}