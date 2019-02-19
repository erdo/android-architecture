---
title: Tutorial: android architecture blueprints, full todo app (MVO edition)
published: false
description:  We take the Android Architecture Blueprints todo app and re-write in MVO using the fore library
cover_image: https://thepracticaldev.s3.amazonaws.com/i/ivyftij3ctd7ee5u1146.png
tags: Android, Java, fore, MVO
series: android fore tutorials
---

**[Difficulty: 4/5]**

The [Android Architecture Blueprints](https://github.com/googlesamples/android-architecture) showcase different android architectures by implementing the same to-do type app, multiple times (once for each architecture variant).

In this post we throw another architecture into the mix: [MVO](https://erdo.github.io/android-fore/00-architecture.html#shoom) implemented with [fore](https://erdo.github.io/android-fore/).

![screen shots of the todo app](https://thepracticaldev.s3.amazonaws.com/i/h4tr9itybrd9zdtlo8lq.png)
<figcaption>screen shots of the MVO todo app</figcaption>

Our fork is written in Java, it's based on the reference MVP implementation.

 - it uses **less** code that any other Java implementation (3261 lines)
 - a Kotlin-MVO version would use even less
 - a lot of the remaining code has been moved out of the view layer
 - the structure of the app is arguably a lot clearer.

It's not a perfect comparison though: our MVO version handles mocking the server in a different way, and we have added a basic Dagger 2 implementation for DI (though we've left a pure DI implementation class there so you can compare the two). Our MVO version actually has *_more_* functionality than the original, we support animated list changes using DiffUtil, and there is a robust networking implementation to fetch tasks from a back end (some json hosted at [mocky.io](https://www.mocky.io/)) - **despite this it still clocks in with less code**.

Let's see how we did that...

_(Note: there are a lot of references to **Task** in the following code, in this situation a task means a real world physical task, like a chore, or a todo item. It's got nothing to do with Android Task or AsyncTask.)_

---

## Original MVP package structure

![original mvp package structure](https://thepracticaldev.s3.amazonaws.com/i/88w3z1nf5fh97l64emhs.png)
<figcaption>original package structure</figcaption>

The package structure of the MVP version mixes concepts slightly: **data** and **util** are self-explanatory, but **addedittask**, **statistics**, **taskdetail**, **tasks** all refer to sections of the app's UI, (they map to the add/edit screen, the statistics screen, and so on).

![mvp example ui package contents](https://thepracticaldev.s3.amazonaws.com/i/98a0vz6jbxeueuke8s6u.png)
<figcaption>view layer package contents</figcaption>

Indeed, inside those UI packages we see android UI stuff like Activities, Fragments, plus the usual MVP classes: the Contract and Presenter.

The overall structure makes it look as if the entire app is its UI (plus the data and a few utility methods). In terms of separating the view layer from the rest of the app, we can do a lot better than this.

(_Presenters are written for the specific views they drive, but in most apps **even this small one** the views are different windows onto the same data, and a lot of what happens in presenter classes is repeated in multiple presenters. One of the things that MVO does is to move this code closer toward the application and away from specific views that might want to use it. This means it can be written and tested once, dramatically improving DRY, and still supporting multiple, thinner views, which are easier to change._)

## MVO package structure

![new mvo package structure](https://thepracticaldev.s3.amazonaws.com/i/s2th1unloyhmgl65ypzf.png)
<figcaption>new package structure</figcaption>

For the MVO implementation, we've split the *data* package into **api** and **db** (it's useful to keep these two separate, for instance it lets us handle minor api changes without affecting our db model too much).

We'll leave the **util** package as it is - it's not central to our discussion.

**message** contains our globally applicable application messages like ERROR_BUSY or ERROR_SESSION_TIMED_OUT - we don't really want anything outside of our api package to know about things like HTTP error codes or networking exceptions. The code in the api package handles the mapping between HTTP and these messages for us without letting any of the networking details leak into the rest of the app.

Everything above is a good idea in my opinion, but not specific to MVO, the next two packages are what really defines an MVO structure though: **ui** and **feature**...

### ui package
![new mvo view layer packages](https://thepracticaldev.s3.amazonaws.com/i/pmpl7dkr1kqvcikikkog.png)
<figcaption>view layer packages</figcaption>

The **ui** package is where you will find the **addedit**, **statistics**, **taskdetail**, and **tasks** sub-packages which map to the screens of the app. Everything here is closely related to the UI and therefore the Android framework, code here is difficult and slow to test, so we want to make it as thin and as simple as we can. So here you'll find the Activity and Fragment classes, plus occasionally Adapters, or any other class directly related to that specific view. There are no Presenter or Contract classes required here though.

### feature package
Now for the **feature** package. If you imagine the app as existing in its own right, without reference to any specific UI, then the feature package is where the bulk of it would reside.

For this small app there is only one "feature" - todo/task management, so there is only one sub-package here: **tasks**. Most commercial apps are going to have a lot more features here, typical examples would be things like: *account*, *shoppingbasket*, *favourites*, *chat*, *loyalty*, *inbox*, *playlists* etc.

This is where the [models](https://erdo.github.io/android-fore/02-models.html#shoom) in [MVO](https://erdo.github.io/android-fore/00-architecture.html#shoom) live. It's the business logic of the app, and the code here should be easy to unit test: these classes should know as little as possible about Android, contexts and certainly not know anything about fragment lifecycles etc. You can refer to the [fore docs](https://erdo.github.io/android-fore/02-models.html#shoom) for complete guidelines about how to write these models - it's standard advice that also applies to writing ViewModels.

---

## Tasks feature

![tasks feature main classes for the MVO implementation](https://thepracticaldev.s3.amazonaws.com/i/apkp48w2xpr05atjhkd2.png)
<figcaption>tasks feature, MVO implementation</figcaption>

This is a substantial re-write of the code that exists in the MVP implementation.


### TaskItem

This is the app's definition of a task.

- It looks similar, but not the same as the task class in the **api** package that we deliberately called [**TaskItemPojo**](https://github.com/erdo/android-architecture/blob/todo-mvo/todoapp/app/src/main/java/com/example/android/architecture/blueprints/todoapp/api/tasks/TaskItemPojo.java) so that we don't get confused.

- It looks similar, but not the same as the task class found in the **db** package that we deliberately called [**TaskItemEntity**](https://github.com/erdo/android-architecture/blob/todo-mvo/todoapp/app/src/main/java/com/example/android/architecture/blueprints/todoapp/db/tasks/TaskItemEntity.java), again so that we don't get confused.

_(This might seem like a lot of effort, and you could write a single task class that satisfies all the requirements of your api, your database model, and your feature if you want - things can get complicated when you have changing requirements and apis though, so just be aware of the tradeoffs here)_

Here's the rest of the feature:

![tasks feature](https://thepracticaldev.s3.amazonaws.com/i/sox9itpxdxxiwuqrctgv.png)
<figcaption>tasks feature</figcaption>


### TaskFetcher

All this does is: connect to the network, fetch tasks from a back end, and add them to local storage.

A lot of the work associated with connecting to a network: parsing responses; handling errors; threading; etc is handled by fore's [CallProcessor](https://erdo.github.io/android-fore/04-more-fore.html#retrofit-and-the-callprocessor) which is a thin wrapper over Retrofit and OkHttp. We pass the downloaded tasks straight to the **TaskListModel** which handles the database work. So all we're left with in this class is a little business logic.

It's **observable**, so it will let any observers know when its state has changed (e.g. when isBusy() switches back to false).

The full code is [here](https://github.com/erdo/android-architecture/blob/todo-mvo/todoapp/app/src/main/java/com/example/android/architecture/blueprints/todoapp/feature/tasks/TaskFetcher.java).


### TaskListModel

This class wraps the database and takes care of all the threading so that the view layer doesn't need to worry about it. Any access to the database goes through here so that the db is transparent to the rest of the app.

This class is designed to support an Android adapter so it includes public methods like **size()** and **get()**.

This class is also **observable**, so any observers will be informed of any changes to the task list. As we are wrapping a Room db, we simply hook into Room's own **InvalidationTracker** for this, which is directly analogous to fore's Observers.

**Animated list updates**: because we plan to animate changes to the list of tasks, we implement the Diffable interface here (that's a small helper from **fore** that let's us automate most of the complication of using DiffUtil).

The full code is [here](https://github.com/erdo/android-architecture/blob/todo-mvo/todoapp/app/src/main/java/com/example/android/architecture/blueprints/todoapp/feature/tasks/TaskListModel.java).


### CurrentTaskModel

This class drives any views that are related to a specific task (currently **taskdetail** and **addedit**).

For this it has public methods like **setTitle()**, **getDescription()**, **saveChanges()** etc

As with the other models, it is written with the assumption that all the methods will be called on the same thread. Any threading is managed internally away from the view layer.

And again it's observable, so that any observers know when to sync their view.

---

## Fixing the view layer

Now we're on to the easy bit! Take a look at some of the set / show methods that exist in the old view layer code:

``` java
@Override
public void setLoadingIndicator(final boolean active) {
  if (getView() == null) {
    return;
  }
  final SwipeRefreshLayout srl =
    (SwipeRefreshLayout) getView().findViewById(R.id.refresh_layout);

  // Make sure setRefreshing() is called after the layout is done with everything else.
  srl.post(new Runnable() {
    @Override
    public void run() {
      srl.setRefreshing(active);
    }
  });
}

@Override
public void showTasks(List<Task> tasks) {
  mListAdapter.replaceData(tasks);
  mTasksView.setVisibility(View.VISIBLE);
  mNoTasksView.setVisibility(View.GONE);
}

@Override
public void showNoActiveTasks() {
  showNoTasksViews(
    getResources().getString(R.string.no_tasks_active),
    R.drawable.ic_check_circle_24dp,
    false
  );
}

```

Even for simple UIs, code like this can get very complicated and can cause subtle, hard to spot bugs in the UI. Here is the full list of methods needed to support just the main tasks UI:


``` java
@Override
public void setLoadingIndicator(final boolean active) {...}

@Override
public void showTasks(List<Task> tasks) {...}

@Override
public void showNoActiveTasks() {...}

@Override
public void showNoTasks() {...}

@Override
public void showNoCompletedTasks() {...}

@Override
public void showSuccessfullySavedMessage() {...}

private void showNoTasksViews(String mainText, int iconRes, boolean showAddView) {...}

@Override
public void showActiveFilterLabel() {...}

@Override
public void showCompletedFilterLabel() {...}

@Override
public void showAllFilterLabel() {...}

@Override
public void showAddTask() {...}

@Override
public void showTaskDetailsUi(String taskId) {...}

@Override
public void showTaskMarkedComplete() {...}

@Override
public void showTaskMarkedActive() {...}

@Override
public void showCompletedTasksCleared() {...}

@Override
public void showLoadingTasksError() {...}
```

The MVO syncView() convention is about to let us delete all of these methods. Everything above can be written as:

``` java
@Override
public void syncView() {
  tasksView.setVisibility(taskListModel.hasVisibleTasks() ? View.VISIBLE :View.GONE);

  noTasksView.setVisibility(taskListModel.hasVisibleTasks() ? View.GONE :View.VISIBLE);
  noTaskMsg.setText(taskListModel.getCurrentFilter().noTasksStringResId);
  noTaskIcon.setImageDrawable(getResources().getDrawable(taskListModel.getCurrentFilter().noTasksDrawableResId));
  noTaskAddView.setVisibility(taskListModel.hasVisibleTasks() ? View.GONE : View.VISIBLE);
  filteringLabelView.setText(getResources().getString(taskListModel.getCurrentFilter().labelStringResId));
  swipeRefreshLayout.setRefreshing(taskFetcher.isBusy());

  listAdapter.notifyDataSetChangedAuto();
}
```

By the time we have done all the views, we have removed a lot of unnecessary code.

The power of the syncView() convention is discussed at length [here](https://erdo.github.io/android-fore/03-reactive-uis.html#syncview). If you're familiar with MVI, it has similar purpose to the render() function.

### Animated list changes

Did you spot the **notifyDataSetChangedAuto()**? (rather than the more usual notifyDataSetChanged()) - this is fore's way of supporting animated list changes, in this case it's backed by Android's DiffUtil but there is another more performant version that you can use for a simple in memory list demonstrated [here](https://erdo.github.io/android-fore/#fore-3-adapter-example). Either way it's a simple call to notifyDataSetChangedAuto() from within your syncView().

_At this point you might be thinking that **fore** must be some huge complicated library to support all this, actually it's [tiny](https://erdo.github.io/android-fore/#method-counts). A lot of the power comes from the MVO concept itself._

### Rotation support

![gif showing the app rotating](https://thepracticaldev.s3.amazonaws.com/i/vfb6sq68yym50ihvgheo.gif)
<figcaption>rotation support as standard</figcaption>

It wouldn't be MVO if **rotation support** and **testability** didn't come as standard.

---

## Testing

Some of the original tests work with no changes necessary, some have been tweaked, and others have had to be re-written. The testing for the MVO app is slightly more focussed on the feature package and uses plain JUnit tests. But there are still plenty of Android UI tests though (for these we use a Dagger2 TestAppModule to mock the models driving the view layer - but a PureDI solution would work just as well)

---

## Things we haven't really improved...

Our view layer now looks much thinner, but we still need quite a bit of boiler plate in the activity classes to support things like the ActionBar, NavigationView, PopUpMenu and handling the options menu. That's just how Android has been designed unfortunately - there is a limit to how much we can avoid these native classes and the boiler plate that comes with them.

Single Activity apps and Google's [Navigation Component](https://developer.android.com/topic/libraries/architecture/navigation/) _may_ be offering a way out of this, or [maybe not](https://proandroiddev.com/why-i-will-not-use-architecture-navigation-component-97d2ad596b36). (If you're new to Android development, you'll quickly learn to take Google's recommendations with a pinch of salt. They are just trying to work things out, the same as the rest of us - sometimes it's helpful, sometimes less so.)

The nice thing about MVO is that it removes so much code from the view layer, it's not so difficult to completely re-write that view layer (using a new navigation structure for instance) and barely have to touch the rest of the app code.

-----

Thanks for reading! If you're thinking of using fore in your team, the fore docs have most of the basics covered in easy to digest sample apps, e.g. [adapters](https://erdo.github.io/android-fore/#fore-3-adapter-example), [networking](https://erdo.github.io/android-fore/#fore-4-retrofit-example) or [databases](https://erdo.github.io/android-fore/#fore-6-db-example-room-db-driven-to-do-list).

here’s the [complete code](https://github.com/erdo/android-architecture) for our MVO fork.
