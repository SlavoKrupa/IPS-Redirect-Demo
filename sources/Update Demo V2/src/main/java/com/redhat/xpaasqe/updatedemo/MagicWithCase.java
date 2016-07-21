package com.redhat.xpaasqe.updatedemo;

/**
 * @author Slavomir Krupa (skrupa@redhat.com)
 */
public final class MagicWithCase {
   private MagicWithCase() {

   }

   public static String magic(String text) {
      return text.toLowerCase();
   }
}
