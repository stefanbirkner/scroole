# Scroole

[![Build Status](https://travis-ci.org/stefanbirkner/scroole.svg?branch=master)](https://travis-ci.org/stefanbirkner/scroole)

Scroole is a Maven plugin that generates code for
[value objects](http://martinfowler.com/bliki/ValueObject.html). The
classes are defined by a simple file that lists the object's fields.

Scroole is published under the
[MIT license](http://opensource.org/licenses/MIT). It requires Java 7,
because it uses [JavaPoet](https://github.com/square/javapoet) that
needs Java 7.


## Usage

First you add the plugin to your `pom.xml` like this:

    <build>
      <plugins>
        <plugin>
          <groupId>com.github.stefanbirkner</groupId>
          <artifactId>scroole-maven-plugin</artifactId>
          <version>0.1.0</version>
        </plugin>
      </plugins>
    </build>

Then you write a file that lists the fields of your class. Suppose you
want to create a class `com.github.stefanbirkner.CheckList`. Create a
file `src/main/java/com/github/stefanbirkner/CheckList.scroole`.

    title: String
    items: java.util.List<com.github.stefanbirkner.CheckListItem>

Generate the class by running

    mvn generate-sources

(The class is generated with `mvn compile`, `mvn test`, ..., too.)

Scroole creates a class `CheckList` that looks like this:

    package com.github.stefanbirkner;

    import java.lang.Object;
    import java.lang.Override;
    import java.lang.String;
    import java.util.List;

    public class CheckList {
      private final String title;

      private final List<CheckListItem> items;

      public CheckList(String title, List<CheckListItem> items) {
        this.title = title;
        this.items = items;
      }

      public String getTitle() {
        return title;
      }

      public List<CheckListItem> getItems() {
        return items;
      }

      @Override
      public int hashCode() {
        int prime = 31;
        int result = 1;
        result = prime * result + (title == null ? 0 : title.hashCode());
        result = prime * result + (items == null ? 0 : items.hashCode());
        return result;
      }

      @Override
      public boolean equals(Object other) {
        if (other == this)
          return true;
        else if (other == null || getClass() != other.getClass())
          return false;
        CheckList that = (CheckList) other;
        return equals(title, that.title)
                && equals(items, that.items);
      }

      private boolean equals(Object left, Object right) {
        if (left == null)
          return right == null;
        else
          return left.equals(right);
      }
    }

## Contributing

You have three options if you have a feature request, found a bug or
simply have a question about Scroole.

* [Write an issue.](https://github.com/stefanbirkner/scroole/issues/new)
* Create a pull request. (See [Understanding the GitHub Flow](https://guides.github.com/introduction/flow/index.html))
* [Write an email to mail@stefan-birkner.de](mailto:mail@stefan-birkner.de)


## Development Guide

Scroole is build with [Maven](http://maven.apache.org/). If you want to
contribute code than

* Please write a test for your change.
* Ensure that you didn't break the build by running `mvn verify -Dgpg.skip`.
* Fork the repo and create a pull request. (See [Understanding the GitHub Flow](https://guides.github.com/introduction/flow/index.html))

The basic coding style is described in the
[EditorConfig](http://editorconfig.org/) file `.editorconfig`.

Scroole supports [Travis CI](https://travis-ci.org/) for continuous
integration. Your pull request will be automatically build by Travis
CI.


## Release Guide

* Select a new version according to the
  [Semantic Versioning 2.0.0 Standard](http://semver.org/).
* Set the new version in `pom.xml` and in the `Installation` section of
  this readme.
* Commit the modified `pom.xml` and `README.md`.
* Run `mvn clean deploy` with JDK 7.
* Add a tag for the release: `git tag scroole-X.X.X`
