package org.spincast.realworld.models.articles;

import java.util.ArrayList;
import java.util.List;

import org.spincast.realworld.exceptions.ValidationError;
import org.spincast.realworld.exceptions.ValidationErrorsException;
import org.spincast.realworld.services.UserService;
import org.spincast.shaded.org.apache.commons.lang3.StringUtils;

import com.google.inject.Inject;

public class ArticleValidator {

    private final UserService userService;

    @Inject
    public ArticleValidator(UserService userService) {
        this.userService = userService;
    }

    protected UserService getUserService() {
        return this.userService;
    }

    /**
     * Validate an {@link Article}.
     *
     * @throws {@link ValidationErrorsException} if something is
     * not valid.
     */
    public void validateArticle(Article article) {

        List<ValidationError> errors = new ArrayList<>();

        if (article.getTagList().size() > 64) {
            errors.add(new ValidationError("tags", "Maximum 64 tags for an article: " + article.getTagList().size()));
        }

        if (StringUtils.isBlank(article.getTitle())) {
            errors.add(new ValidationError("title", "The title is required"));
        } else if (article.getTitle().length() > 255) {
            errors.add(new ValidationError("title",
                                           "The title must have 255 characters maximum. Currently: " +
                                                    article.getTitle().length() + " characters"));
        }

        if (StringUtils.isBlank(article.getDescription())) {
            errors.add(new ValidationError("description", "The description is required"));
        } else if (article.getDescription().length() > 1024) {
            errors.add(new ValidationError("description",
                                           "The description must have 1024 characters maximum. Currently: " +
                                                          article.getDescription().length() + " characters"));
        }

        if (StringUtils.isBlank(article.getBody())) {
            errors.add(new ValidationError("body", "The body is required"));
        } else if (article.getBody().length() > 10000) {
            errors.add(new ValidationError("body",
                                           "The body must have 10000 characters maximum. Currently: " +
                                                   article.getBody().length() + " characters"));
        }

        List<String> tagsErrors = new ArrayList<>();
        for (int i = 0; i < article.getTagList().size(); i++) {
            String tag = article.getTagList().get(i);
            if (StringUtils.isBlank(tag)) {
                tagsErrors.add("Tag " + (i + 1) + " can't be empty");
            } else if (tag.length() > 255) {
                tagsErrors.add("Tag " + (i + 1) + " contains " + tag.length() + " characters. The maximum is 255.");
            }
        }
        if (tagsErrors.size() > 0) {
            errors.add(new ValidationError("tags", tagsErrors));
        }

        if (errors.size() > 0) {
            throw new ValidationErrorsException(errors);
        }
    }
}
