// Copyright 2017 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package codeu.chat.util;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public abstract class SerializedExecutor<T> implements Runnable {

  private final BlockingQueue<T> values = new LinkedBlockingQueue<>();

  public void add(T value) {
    while (!values.offer(value)) {
      // Keep trying to add the value. As the queue is a linked blocking queue, this
      // should never happen.
    }
  }

  public void run() {
    for (T value = next(); value != null; value = next()) {
      onValue(value);
    }
  }

  private T next() {
    while (true) {
      try {
        return values.take();
      } catch (InterruptedException ex) {
        return null;
      }
    }
  }

  protected abstract void onValue(T value);
}
