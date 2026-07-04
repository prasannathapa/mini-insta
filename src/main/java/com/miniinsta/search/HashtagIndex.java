package com.miniinsta.search;

import java.util.List;
import java.util.Set;

/**
 * Port for an inverted index: tag -&gt; the posts that used it. This is the same
 * idea a dedicated search service (Elasticsearch, etc.) provides at scale; here
 * it is a map, but the seam is the point.
 */
public interface HashtagIndex {

    void index(long postId, Set<String> tags);

    /** Post ids for a tag, most recently indexed first. */
    List<Long> postsFor(String tag);
}
