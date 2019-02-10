---
title: Tutorial: android fore basics
published: true
description: develop a basic slot machine game
cover_image: https://thepracticaldev.s3.amazonaws.com/i/b6bgzs5na0xrag5a6b8b.png
tags: Android, Kotlin, fore, MVO
series: android fore tutorials
---

The [Android Architecture Blueprints](https://github.com/googlesamples/android-architecture) repo showcases different architectures by implementing the same to-do type app multiple times (once for each architecture variant).

It's a great place to check how common tasks can be tackled by an architecture. (Bare in mind there is nothing particularly magical about it just because it is an official Google repo - it's written by developers like you and me: some of the code is great, some of the code is less great).

In this post we are going to through another architecture into the mix: [MVO](https://erdo.github.io/android-fore/00-architecture.html#shoom) implemented with [fore](https://erdo.github.io/android-fore/).

Our fork is written in Java, it's based on the reference MVP implementation of the Android Architecture Blueprints app. It stands up pretty well in comparison with the other implementations (in fact I believe it uses less code than any of the Java implementations in that repo). A Kotlin version would be even smaller.

It's not a perfect comparison though: the MVO version actually has a tiny bit _more_ functionality; we handle mocking the server in a different way; and the package structure of MVO is significantly different, it focusses much more on separating the UI layer from the rest of the app. I think it does a good job of taking as much code out of the view layer as possible (as it should do, because that's the main point of **fore**).


Let's highlight the main points for this dev.to post (or we'll be here all day)







 It does however represent a large amount of time and effort, and some contrifutors are doing it for free).


, and that means  and just because an implementation is present in the repo, doesn't mean that's the only way to do it or even that it's the best way to do it.


  to look up how people tend to
There are a lot of branches and I don't recommend studying each one

This is the MVO version written with the help of the fore library.


Let’s write a simple slot machine game. We’ll use the tiny [**fore**](https://erdo.github.io/android-fore/) library to demonstrate:

- a way to truly **separate your app from its UI layer**
- a way to get rock solid **UI consistency**
- a way to handle **asynchronous code** with no memory leaks

![unlikely slot machine game](https://thepracticaldev.s3.amazonaws.com/i/2fxfrc02wlg7z3yzvgyd.png)
<figcaption>unlikely slot machine game</figcaption>

Because we’re using fore: **rotation support** and **testability** will drop out at the end automatically, and we will also be writing surprisingly little code.

We’ll write this one in Kotlin, because Kotlin.

-----

_**More simple**: For even simpler examples see the fore github repo. It comes with a number of tiny app examples covering just: [reative-ui](https://erdo.github.io/android-fore/#fore-1-reactive-ui-example) basics, [asynchronous code](https://erdo.github.io/android-fore/#fore-2-asynchronous-code-example), [adapters](https://erdo.github.io/android-fore/#fore-3-adapter-example), [networking](https://erdo.github.io/android-fore/#fore-4-retrofit-example) (with Retrofit2), [animations and lifecycle](https://erdo.github.io/android-fore/#fore-5-ui-helpers-example-tic-tac-toe), and [db driven apps](https://erdo.github.io/android-fore/#fore-6-db-example-room-db-driven-to-do-list) (with Room)_

_**More complicated**: A lot of architectures start to show the strain once you move beyond trivial complexity. Complexity at the UI layer is something that **fore** handles particularly well, and there is a larger more complex app code base [here](https://github.com/erdo/fore-full-example-01-kotlin) to help you investigate that_

-----

## Let’s get started

![MVO](https://thepracticaldev.s3.amazonaws.com/i/nhwuewbqjn1e6rdkvwq0.png)

**fore** implements the [Model View Observer](https://erdo.github.io/android-fore/00-architecture.html#shoom) architecture, we'll tackle each component in turn:

## Model
We need a model to drive our slot machine, we’re going to call it **SlotMachineModel**. It’s going to have a **spin()** method which will set the wheels spinning. The 3 wheels are going to either be spinning or showing a cherry, a dice, or a bell. (Ok so I’ve never actually played a real slot machine, sucks to be me ;p).

Let’s keep it simple and have three methods called: **getState1()**, **getState2()**, **getState3()**. And they can return an enum for each wheel with one of these states: **SPINNING**, **CHERRY**, **DICE**, **BELL**

We’ll also have an **isWon()** method that’s always going to return false, unless the three wheels are: **ALL** identical **AND NOT** SPINNING (eg CHERRY, CHERRY, CHERRY would be a win)

To build up the suspense (and let you check rotation support for yourselves) we will do the spinning calculations *asynchronously* and we will take our time about it, by randomly adding a few seconds delay to each spin.

Last but by no means least: the **Model needs to be Observable**, whenever this model’s state changes, it needs to let its observers know that it’s changed. (You might want to put your phone in landscape for the code bit if you're not at your desk)

``` kotlin
class SlotMachineModel constructor(
        private val stateFetcher: RandomStateFetcher,
        private val workMode: WorkMode) : ObservableImp(workMode) {

    enum class State {
        SPINNING,
        CHERRY,
        DICE,
        BELL
    }

    private val rnd = Random()
    private val wheel1 = Wheel(State.CHERRY)
    private val wheel2 = Wheel(State.DICE)
    private val wheel3 = Wheel(State.BELL)

    fun spin() {
        spinWheel(wheel1)
        spinWheel(wheel2)
        spinWheel(wheel3)
    }

    private fun spinWheel(wheel: Wheel) {

        //if wheel is already spinning, just ignore
        if (wheel.state != State.SPINNING) {
            wheel.state = State.SPINNING
            notifyObservers()
            AsyncBuilder<Void, State>(workMode)
                .doInBackground {
                    stateFetcher.fetchRandom(randomDelayMs())
                }
                .onPostExecute { state ->
                    wheel.state = state; notifyObservers()
                }
                .execute()
        }
    }

    fun getState1(): State {
        return wheel1.state
    }

    fun getState2(): State {
        return wheel2.state
    }

    fun getState3(): State {
        return wheel3.state
    }

    fun isWon(): Boolean {
        return (wheel1.state == wheel2.state
                && wheel2.state == wheel3.state
                && wheel1.state != State.SPINNING)
    }

    //anywhere between 1 and 10 seconds
    private fun randomDelayMs(): Long {
        return (1000
              + rnd.nextInt(8) * 1000
              + rnd.nextInt(1000)).toLong()
    }

    internal inner class Wheel(var state: State)
}

```

That should do for our model, a few things to notice:

- We are calling **notifyObservers()** immediately, whenever any of our model’s state changes
- We are passing in a **WorkMode** dependency that controls how the notifications are sent and makes things easy to test
- Everything is designed to run on the **UI thread** apart from when we explicitly jump to a background thread to fetch the wheel states (this is where networking usually happens)
- We’re using **fore**’s **AsyncBuilder** for that (which is just a wrapper around AsyncTask that supports lambdas). This lets us easily run the code in Synchronous mode if we want to (e.g. for tests), but you could use RxJava or coroutines for this too.
- This Model knows nothing about android view layer classes or contexts
- We’re also using a RandomStateFetcher dependency to fetch the actual state (nothing to do with fore, this just makes things easier to test, it’s also where you might put networking code in future, see [here](https://erdo.github.io/android-fore/#fore-4-retrofit-example) for a simple app with a networking layer)

Much more information and a big checklist for writing models when you’re using fore is [here](https://erdo.github.io/android-fore/02-models.html#shoom)

## View

We’ll write a custom view class to display our slot machine, we’re going to call it **SlotMachineView**, and it’s NOT going to be in the same package as the model.

We can do it in a standard XML layout as follows, the root of this one happens to be RelativeLayout, so our SlotMachineView will extend that.

``` xml
<?xml version="1.0" encoding="utf-8"?>
<foo.bar.example.foreintro.ui.SlotMachineView
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:padding="@dimen/slot_spacer">

    <ImageView
        android:id="@+id/slots_machine"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_centerInParent="true"
        android:layout_marginLeft="@dimen/slot_spacer"
        android:layout_marginStart="@dimen/slot_spacer"
        android:contentDescription="slot machine background"
        android:scaleType="fitCenter"
        android:src="@drawable/machine" />

    <!-- central slot -->

    <foo.bar.example.foreintro.ui.widget.SlotView
        android:id="@+id/slots_2_slotview"
        android:layout_width="@dimen/slot_icon_size"
        android:layout_height="@dimen/slot_icon_size"
        android:layout_centerInParent="true"
        android:background="@drawable/slots_dice"
        android:contentDescription="2nd slot" />

    <!-- left most slot -->

    <foo.bar.example.foreintro.ui.widget.SlotView
        android:id="@+id/slots_1_slotview"
        android:layout_width="@dimen/slot_icon_size"
        android:layout_height="@dimen/slot_icon_size"
        android:layout_centerVertical="true"
        android:layout_toLeftOf="@+id/slots_2_slotview"
        android:background="@drawable/slots_bell"
        android:contentDescription="1st slot" />

    <!-- right most slot -->

    <foo.bar.example.foreintro.ui.widget.SlotView
        android:id="@+id/slots_3_slotview"
        android:layout_width="@dimen/slot_icon_size"
        android:layout_height="@dimen/slot_icon_size"
        android:layout_centerVertical="true"
        android:layout_toRightOf="@+id/slots_2_slotview"
        android:background="@drawable/slots_cherry"
        android:contentDescription="3rd slot" />

    <!-- slot machine handle -->

    <ImageView
        android:id="@+id/slots_handle_imageview"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_centerVertical="true"
        android:layout_toRightOf="@+id/slots_machine"
        android:src="@drawable/slot_handle_selector" />

    <!-- slot machine win banner -->

    <ImageView
        android:id="@+id/slots_win"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_alignParentTop="true"
        android:layout_centerHorizontal="true"
        android:src="@drawable/slots_win" />

</foo.bar.example.foreintro.ui.SlotMachineView>
```
Our custom view is going to handle the following standard things:

- get a **reference to all the view elements** we need
- get a reference to the **SlotMachineModel** which will inform what state is displayed on the UI
- hook up the **button listeners** so that they interact with the model
- **sync our view** so that it matches the state of the SlotMachineModel (set all the SlotView backgrounds, and the win banner each time the model notifies us that it’s changed)

``` kotlin
class SlotMachineView @JvmOverloads constructor(
        context: Context?,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
        defStyleRes: Int = 0) :
        RelativeLayout(context, attrs, defStyleAttr), SyncableView {


    //models that we need
    private lateinit var slotMachineModel: SlotMachineModel

    //single observer reference
    internal var observer = this::syncView


    override fun onFinishInflate() {
        super.onFinishInflate()

        //(getting view references is handled for us by kotlin tools)

        getModelReferences()

        setClickListeners()
    }


    private fun getModelReferences() {
        slotMachineModel = App.get(SlotMachineModel::class.java)
    }

    private fun setClickListeners() {
        slots_handle.setOnClickListener { slotMachineModel.spin() }
    }


    //fore data binding stuff below

    override fun syncView() {
        slots_1_slotview.setState(slotMachineModel.getState1())
        slots_2_slotview.setState(slotMachineModel.getState2())
        slots_3_slotview.setState(slotMachineModel.getState3())
        slots_win.visibility = if (slotMachineModel.isWon())
                               View.VISIBLE else View.INVISIBLE
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        slotMachineModel.addObserver(observer)
        syncView() //  <- don't forget this
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        slotMachineModel.removeObserver(observer)
    }
}
```

By the way, that syncView() method is surprisingly powerful as long as you stick to 2 rules when you write it:

-If a model being observed changes **in any way**, then the **entire** view is refreshed.
-Where there is an **if**, there must also be an **else**

That’s explained in [detail here](https://erdo.github.io/android-fore/03-reactive-uis.html#syncview)

## Observer

This is the last part of implementing MVO, and we’ve kind of already done it.

All the **Model** needed to do to become observable was to extend **fore**’s **ObservableImp** *(or to implement the Observable interface)* and to make sure to call **notifyObservers()** each time any of the state changed.

For the **View**, all it had to do to observe the Model was to **add** and **remove** its Observable in line with the Android lifecycle methods, and to call **syncView()** whenever the model notified it of a change.

Threading issues are taken care of here as everything operates on the UI thread (including the observer notifications) which is exactly how you need UI code to be run anyway. Asynchronous code is run away from the UI layer, inside the model and is completed before the lightweight notifications are fired in the UI thread (this is also how **fore** is able to support [adapters](https://erdo.github.io/android-fore/04-more-fore.html#adapters-notifydatasetchangedauto) in a very similar manner).

We didn’t mention [DI](https://erdo.github.io/android-fore/05-extras.html#dependency-injection-basics) yet, but it’s key. The line you see in the View:

``` kotlin
slotMachineModel = App.get(SlotMachineModel::class.java)
```
provides the view with the same instance of the model each time, so that everything remains consistent, even when you rotate the screen. You might use Dagger2 for that, the example here uses a pure DI solution.

![automatically supports rotation](https://thepracticaldev.s3.amazonaws.com/i/0s05vx6ror703z1h074x.gif)
<figcaption>as promised… this already supports rotation</figcaption>

For completeness and so that you can see how tiny it is, this is the [activity code](https://github.com/erdo/fore-intro-tutorial/blob/master/app/src/main/java/foo/bar/example/foreintro/ui/MainActivity.kt) we use to launch the view

## Testing

Because there is so much separation between the view layer and the rest of the app, this code is particularly testable. The main [fore repo](https://erdo.github.io/android-fore/) has a lot of sample apps, many of which are tested extensively if want to study test examples.

## Now for the really cool stuff…

This is a simple example: one view observing one model, sooooo what.

But there’s something clever in that very basic looking observer code. It has no parameter. This means that if your view suddenly needs to observe not just a **SlotMachineModel**, but also a **UserRepository**, or an **AccountModel**, or **NetworkStatusModel**, **ChatInboxModel**, **WeatherModel**— or all 6 models at the same time. It can. Easily. The observer interface is identical for all the models, so all a view needs to do is to get hold of a reference to the models using DI, and then:

``` kotlin
override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    slotMachineModel.addObserver(observer)
    userRepository.addObserver(observer)
    accountModel.addObserver(observer)
    networkStatusModel.addObserver(observer)
    chatInboxModel.addObserver(observer)
    weatherModel.addObserver(observer)
    syncView() //  <- don't forget this
}

override fun onDetachedFromWindow() {
    super.onDetachedFromWindow()
    slotMachineModel.removeObserver(observer)
    userRepository.removeObserver(observer)
    accountModel.removeObserver(observer)
    networkStatusModel.removeObserver(observer)
    chatInboxModel.removeObserver(observer)
    weatherModel.removeObserver(observer)
}
```

And if you have a strong objection to that level of boiler plate, there are a few [lifecycle classes](https://erdo.github.io/android-fore/04-more-fore.html#lifecycle-components) available in fore that will do it for you ;)

As for the Models themselves, they don’t even know how many views might be observing them, it makes no difference to their code at all.

Let’s say the view wants to: _disable the slot machine handle if the account has no money in it_. **But, we want to remain reactive and keep that enabled state correct when the balance increases or decreases, or if the phone screen rotates etc.** One line inside syncView:

``` kotlin
slots_handle.isEnabled = accountModel.balance > 0
```

Maybe the view should: _show a weather forecast text at the top of the screen_. **But again we need to keep our UI nice and reactive so that if the weather forecast changes or the phone is rotated, everything remains consistent**. One extra line in syncView:


``` kotlin
weather_text.text = weatherModel.getForecastText()
```

That text should probably: _be red if the windspeed is above 50mph_. **But we don't want the testers finding any UI consistency bugs when they rotate the phone, or leave the phone in a drawer for a week or whatever else it is they'll try**. One extra line in syncView:

``` kotlin
weather_text.setTextColor(resources.getColor(
                             if (weatherModel.windSpeed>50)
                                   R.color.red else R.color.blue))
```

This code might make things look very easy, but there are many ways to get things wrong of course, if there’s something in your app that doesn’t feel right or you’re having any performance issues - chances are you’ll find some guidance in the check list [here](https://erdo.github.io/android-fore/05-extras.html#troubleshooting--how-to-smash-code-reviews).

-----

Well thanks for reading this far!

Hopefully this gives you some ideas about how you could make use of the **fore** library or just **MVO** to write quicker, cleaner, more testable apps. I use it all the time during my work as it is production ready (it’s a very small library so there is not a lot to go wrong). There are lots more considerations to discuss when you look at [adapters](https://erdo.github.io/android-fore/#fore-3-adapter-example), [animations](https://erdo.github.io/android-fore/#fore-5-ui-helpers-example-tic-tac-toe), [databases](https://erdo.github.io/android-fore/#fore-6-db-example-room-db-driven-to-do-list) etc, but they are all treated in the **same standard way** using the syncView() convention. For the moment the best place to look for clear examples is in the [fore github repo](https://erdo.github.io/android-fore/).

here’s the [complete code](https://github.com/erdo/fore-intro-tutorial) for the tutorial
