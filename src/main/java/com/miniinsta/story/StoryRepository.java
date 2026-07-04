package com.miniinsta.story;

import java.util.Collection;
import java.util.List;

/** Port for storing and retrieving stories. */
public interface StoryRepository {

    Story save(Story story);

    /** All stories authored by any of the given users (active or not). */
    List<Story> byAuthors(Collection<Long> authorIds);
}
