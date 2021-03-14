# todo-mvo


_**Please see the [dev.to article](https://dev.to/erdo/tutorial-android-architecture-blueprints-full-todo-app-mvo-edition-259o) for the full details of this implementation, it's meant for comparison purposes only - if you are looking for a place to start writting an app with fore, the [fore repo](https://erdo.github.io/android-fore/) has much clearer and more up-to-date and examples**_

This version of the android-architecture todo app is written in the [MVO](https://erdo.github.io/android-fore/00-architecture.html#shoom) architectural style. It uses the [android-fore](https://erdo.github.io/android-fore/) library for its Observer implementation. The sample aims to:

* Provide a basic [Model-View-Observer](https://erdo.github.io/android-fore/00-architecture.html#shoom) (MVO) implementation example.
* Act as a reference point for comparing and contrasting the other samples in the android-architecture project.


### What you need

Before exploring this sample, you might find it useful to familiarize yourself with the following topics:

* The [project README](https://github.com/googlesamples/android-architecture/tree/master)
* The [Dev.to tutorial](https://dev.to/erdo/tutorial-android-architecture-blueprints-full-todo-app-mvo-edition-259o) which covers this implementation
* The [MVO](https://erdo.github.io/android-fore/00-architecture.html#shoom) architecture
* The [fore](https://erdo.github.io/android-fore/) library documentation for further reading


### Dependencies

The todo-mvo sample uses the following dependencies:

* **fore** - for its Observer classes and various helpers
* **Retrofit2** - for networking code
* **Room** - for database code
* **Dagger2** - for basic DI
* **Mockito, JUnit, Espresso, Robolectric** - for testing


### Designing the app

![mvo android architecture todo sample app](https://thepracticaldev.s3.amazonaws.com/i/vfb6sq68yym50ihvgheo.gif)

All versions of the Android Blueprints app include the same common features in a simple to-do type app. The app consists of four UI screens:
* Tasks - Used to manage a list of tasks.
* TaskDetail - Used to read or delete a task.
* AddEditTask - Used to create or edit tasks.
* Statistics - Displays statistics related to tasks.

### Code metrics

The table below summarizes the amount of code used to implement this version of the app and tests. You can use it as a basis for comparison with similar tables provided for each of the other samples in this project.

| Language      | Number of files | Blank lines | Comment lines | Lines of code |
| ------------- | --------------- | ----------- | ------------- | ------------- |
| **Java**      |               44|         1099|            986|           3261|
| **XML**       |               37|          105|            339|            671|
| **JSON**      |                3|            0|              0|             21|
| **Total**     |               84|         1204|           1325|           3953|

(the app itself is 1971 lines of java code)

## License


    Copyright 2017-2019 early.co

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
