/**
 *    Copyright 2009-2018 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.reflection;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.*;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import static com.googlecode.catchexception.apis.BDDCatchException.*;
import static org.assertj.core.api.BDDAssertions.then;

public class ReflectorTest {

  @Test
  public void testGetSetterType() throws Exception {
    ReflectorFactory reflectorFactory = new DefaultReflectorFactory();
    Reflector reflector = reflectorFactory.findForClass(Section.class);
    Assertions.assertEquals(Long.class, reflector.getSetterType("id"));
  }

  @Test
  public void testGetGetterType() throws Exception {
    ReflectorFactory reflectorFactory = new DefaultReflectorFactory();
    Reflector reflector = reflectorFactory.findForClass(Section.class);
    Assertions.assertEquals(Long.class, reflector.getGetterType("id"));
  }

  @Test
  public void shouldNotGetClass() throws Exception {
    ReflectorFactory reflectorFactory = new DefaultReflectorFactory();
    Reflector reflector = reflectorFactory.findForClass(Section.class);
    Assertions.assertFalse(reflector.hasGetter("class"));
  }

  static interface Entity<T> {
    T getId();

    void setId(T id);
  }

  static abstract class AbstractEntity implements Entity<Long> {

    private Long id;

    @Override
    public Long getId() {
      return id;
    }

    @Override
    public void setId(Long id) {
      this.id = id;
    }
  }

  static class Section extends AbstractEntity implements Entity<Long> {
  }

  @Test
  public void shouldResolveSetterParam() throws Exception {
    ReflectorFactory reflectorFactory = new DefaultReflectorFactory();
    Reflector reflector = reflectorFactory.findForClass(Child.class);
    assertEquals(String.class, reflector.getSetterType("id"));
  }

  @Test
  public void shouldResolveParameterizedSetterParam() throws Exception {
    ReflectorFactory reflectorFactory = new DefaultReflectorFactory();
    Reflector reflector = reflectorFactory.findForClass(Child.class);
    assertEquals(List.class, reflector.getSetterType("list"));
  }

  @Test
  public void shouldResolveArraySetterParam() throws Exception {
    ReflectorFactory reflectorFactory = new DefaultReflectorFactory();
    Reflector reflector = reflectorFactory.findForClass(Child.class);
    Class<?> clazz = reflector.getSetterType("array");
    assertTrue(clazz.isArray());
    assertEquals(String.class, clazz.getComponentType());
  }

  @Test
  public void shouldResolveGetterType() throws Exception {
    ReflectorFactory reflectorFactory = new DefaultReflectorFactory();
    Reflector reflector = reflectorFactory.findForClass(Child.class);
    assertEquals(String.class, reflector.getGetterType("id"));
  }

  @Test
  public void shouldResolveSetterTypeFromPrivateField() throws Exception {
    ReflectorFactory reflectorFactory = new DefaultReflectorFactory();
    Reflector reflector = reflectorFactory.findForClass(Child.class);
    assertEquals(String.class, reflector.getSetterType("fld"));
  }

  @Test
  public void shouldResolveGetterTypeFromPublicField() throws Exception {
    ReflectorFactory reflectorFactory = new DefaultReflectorFactory();
    Reflector reflector = reflectorFactory.findForClass(Child.class);
    assertEquals(String.class, reflector.getGetterType("pubFld"));
  }

  @Test
  public void shouldResolveParameterizedGetterType() throws Exception {
    ReflectorFactory reflectorFactory = new DefaultReflectorFactory();
    Reflector reflector = reflectorFactory.findForClass(Child.class);
    assertEquals(List.class, reflector.getGetterType("list"));
  }

  @Test
  public void shouldResolveArrayGetterType() throws Exception {
    ReflectorFactory reflectorFactory = new DefaultReflectorFactory();
    Reflector reflector = reflectorFactory.findForClass(Child.class);
    Class<?> clazz = reflector.getGetterType("array");
    assertTrue(clazz.isArray());
    assertEquals(String.class, clazz.getComponentType());
  }

  static abstract class Parent<T extends Serializable> {
    protected T id;
    protected List<T> list;
    protected T[] array;
    private T fld;
    public T pubFld;

    public T getId() {
      return id;
    }

    public void setId(T id) {
      this.id = id;
    }

    public List<T> getList() {
      return list;
    }

    public void setList(List<T> list) {
      this.list = list;
    }

    public T[] getArray() {
      return array;
    }

    public void setArray(T[] array) {
      this.array = array;
    }

    public T getFld() {
      return fld;
    }
  }

  static class Child extends Parent<String> {
  }

  @Test
  public void shouldResoleveReadonlySetterWithOverload() throws Exception {
    class BeanClass implements BeanInterface<String> {
      @Override
      public void setId(String id) {
        // Do nothing
      }
    }
    ReflectorFactory reflectorFactory = new DefaultReflectorFactory();
    Reflector reflector = reflectorFactory.findForClass(BeanClass.class);
    assertEquals(String.class, reflector.getSetterType("id"));
  }

  interface BeanInterface<T> {
    void setId(T id);
  }

  @Test
  public void shouldSettersWithUnrelatedArgTypesThrowException() throws Exception {
    @SuppressWarnings("unused")
    class BeanClass {
      public void setTheProp(String arg) {}
      public void setTheProp(Integer arg) {}
    }

    ReflectorFactory reflectorFactory = new DefaultReflectorFactory();
    when(reflectorFactory).findForClass(BeanClass.class);
    then(caughtException()).isInstanceOf(ReflectionException.class)
      .hasMessageContaining("theProp")
      .hasMessageContaining("BeanClass")
      .hasMessageContaining("java.lang.String")
      .hasMessageContaining("java.lang.Integer");
  }

  @Test
  public void shouldAllowTwoBooleanGetters() throws Exception {
    @SuppressWarnings("unused")
    class Bean {
      // JavaBean Spec allows this (see #906)
      public boolean isBool() {return true;}
      public boolean getBool() {return false;}
      public void setBool(boolean bool) {}
    }
    ReflectorFactory reflectorFactory = new DefaultReflectorFactory();
    Reflector reflector = reflectorFactory.findForClass(Bean.class);
    assertTrue((Boolean)reflector.getGetInvoker("bool").invoke(new Bean(), new Byte[0]));
  }

  @Test
  public void shouldNotAllowTwoNonBooleanGetters() throws Exception {
    class Bean {
      // get方法和is方法只允许出现在boolean属性上
      public Integer isBool() {return 1;}
      public boolean getBool() {return false;}
      public void setBool(boolean bool) {}
    }
    ReflectorFactory reflectorFactory = new DefaultReflectorFactory();
    when(reflectorFactory).findForClass(Bean.class);
    then(caughtException()).isInstanceOf(ReflectionException.class)
            .hasMessageContaining("Illegal overloaded getter method with ambiguous type for property bool")
            .hasStackTraceContaining("resolveGetterConflicts");
  }

  @Test
  public void testNotValidProperty() throws InvocationTargetException, IllegalAccessException {
    class Bean {
      //是不能定义这种方法的
      public boolean isClass(){return false;}
      public boolean get$$Test(){return false;}
      public boolean getSerialVersionUID(){return false;};
    }
    ReflectorFactory reflectorFactory = new DefaultReflectorFactory();
    Reflector reflector = reflectorFactory.findForClass(Bean.class);
    when(reflector).getGetInvoker("class");
    then(caughtException()).isInstanceOf(ReflectionException.class)
            .hasMessageContaining("There is no getter for property named 'class'");

    when(reflector).getGetInvoker("$$Test");
    then(caughtException()).isInstanceOf(ReflectionException.class)
            .hasMessageContaining("There is no getter for property named '$$Test'");

    when(reflector).getGetInvoker("serialVersionUID");
    then(caughtException()).isInstanceOf(ReflectionException.class)
            .hasMessageContaining("There is no getter for property named 'serialVersionUID'");

  }

  @Test
  public void testField() throws InvocationTargetException, IllegalAccessException {
    Field[] fields = Bean2.class.getDeclaredFields();
    Bean2 obj = new Bean2();
    for (Field field : fields) {
      String name = field.getName();
      if("s".equals(name)){
        //static类型的字段可以通过反射修改
        field.set(obj, "1_s");
      } else if("s2".equals(name)){
        //final类型的字段可以通过反射修改
        field.setAccessible(true);
        field.set(obj, "1_s2");
      } else if("s3".equals(name)){
        // static final类型的字段无法通过反射修改，只有classloader可以设置
        field.setAccessible(true);
        field.set(obj, "1_s3");
      }
    }
    System.out.println(obj);
    ReflectorFactory reflectorFactory = new DefaultReflectorFactory();
    Reflector reflector = reflectorFactory.findForClass(Bean2.class);

    reflector.getSetInvoker("s2").invoke(obj, new Object[]{"fff"});
  }

  static class Bean2 {
    static String s = "s";
    final String s2 = "s2";
    static final String s3 = "s3";
  }
}
