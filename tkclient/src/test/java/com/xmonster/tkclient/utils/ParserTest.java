package com.xmonster.tkclient.utils;

import android.text.TextUtils;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.internal.util.collections.Iterables;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Iterator;

import static org.mockito.Matchers.any;

@RunWith(PowerMockRunner.class)
@PrepareForTest(TextUtils.class)
public class ParserTest {

    @Before
    public void setup() {
        PowerMockito.mockStatic(TextUtils.class);
        PowerMockito.when(TextUtils.join(any(CharSequence.class), any(Iterable.class))).thenAnswer(new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocation) throws Throwable {
                StringBuilder sb = new StringBuilder();
                CharSequence delimiter = (CharSequence) invocation.getArguments()[0];
                Iterator<?> it = ((Iterable) invocation.getArguments()[1]).iterator();
                if (it.hasNext()) {
                    sb.append(it.next());
                    while (it.hasNext()) {
                        sb.append(delimiter);
                        sb.append(it.next());
                    }
                }
                return sb.toString();
            }
        });
    }

    @org.junit.Test
    public void testCalc() throws Exception {
        Conditions.Parser.calc("test>1 && (test<2 || uid=100)");
    }
}