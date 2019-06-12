# koroutines
Hi Spring fans! In this week's installment I look at Kotlin's support for coroutines, a new approach to working with asynchronous code using suspendable computations, i.e. the idea that a function can suspend its execution at some point and resume later on.

* koroutines are powered by continuations. If you call a suspedning function, 
  it'll execute the function on another thread and pass in a continuation.