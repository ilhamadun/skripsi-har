cmake_minimum_required(VERSION 3.4.1)

add_library( # Specifies the name of the library.
             classifier

             # Sets the library as a shared library.
             SHARED

             # Provides a relative path to your source file(s).
             classifier.cpp )

target_link_libraries(classifier
                      android
                      log)