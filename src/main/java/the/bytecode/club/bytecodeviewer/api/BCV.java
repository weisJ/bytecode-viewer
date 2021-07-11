package the.bytecode.club.bytecodeviewer.api;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.objectweb.asm.tree.ClassNode;
import the.bytecode.club.bytecodeviewer.compilers.Compiler;
import the.bytecode.club.bytecodeviewer.compilers.InternalCompiler;
import the.bytecode.club.bytecodeviewer.decompilers.InternalDecompiler;
import the.bytecode.club.bytecodeviewer.decompilers.Decompiler;
import the.bytecode.club.bytecodeviewer.plugin.preinstalled.EZInjection;
import the.bytecode.club.bytecodeviewer.util.DialogueUtils;
import the.bytecode.club.bytecodeviewer.util.JarUtils;
import the.bytecode.club.bytecodeviewer.util.MiscUtils;

import javax.swing.*;

import static the.bytecode.club.bytecodeviewer.Constants.*;

/***************************************************************************
 * Bytecode Viewer (BCV) - Java & Android Reverse Engineering Suite        *
 * Copyright (C) 2014 Kalen 'Konloch' Kinloch - http://bytecodeviewer.com  *
 *                                                                         *
 * This program is free software: you can redistribute it and/or modify    *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation, either version 3 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 *   This program is distributed in the hope that it will be useful,       *
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         *
 *   GNU General Public License for more details.                          *
 *                                                                         *
 *   You should have received a copy of the GNU General Public License     *
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>. *
 ***************************************************************************/

/**
 * The official API for BCV, this was designed for plugin authors and developers utilizing EZ-Injection.
 *
 * The BCV Class is meant to to help aid in dynamic analysis and plugin utility.
 *
 * @author Konloch
 */
public class BCV
{
    private static ClassNodeLoader loader = new ClassNodeLoader(); //might be insecure due to assholes targeting BCV
    private static URLClassLoader cl;

    /**
     * Grab the loader instance
     *
     * @return
     */
    public static ClassNodeLoader getClassNodeLoader() {
        return loader;
    }

    /**
     * Returns the URLClassLoader instance
     *
     * @return the URLClassLoader instance
     */
    public static URLClassLoader getClassLoaderInstance() {
        return cl;
    }

    /**
     * Re-instances the URLClassLoader and loads a jar to it.
     *
     * @return The loaded classes into the new URLClassLoader instance
     * @author Cafebabe
     */
    public static Class<?> loadClassIntoClassLoader(ClassNode cn)
    {
        getClassNodeLoader().addClass(cn);
    
        try {
            return cl.loadClass(cn.name);
        } catch (Exception classLoadException) {
            the.bytecode.club.bytecodeviewer.BytecodeViewer.handleException(classLoadException);
        }
        
        return null;
    }
    
    public static List<Class<?>> loadClassesIntoClassLoader()
    {
        try
        {
            File f = new File(tempDirectory + fs + MiscUtils.randomString(12) + "loaded_temp.jar");
            
            JarUtils.saveAsJar(BCV.getLoadedClasses(), f.getAbsolutePath());
            JarFile jarFile = new JarFile("" + f.getAbsolutePath());
            
            Enumeration<JarEntry> e = jarFile.entries();
            URL[] urls = {new URL("jar:file:" + "" + f.getAbsolutePath() + "!/")};
            
            cl = URLClassLoader.newInstance(urls);
            List<Class<?>> ret = new ArrayList<>();

            while (e.hasMoreElements())
            {
                JarEntry je = e.nextElement();
                
                if (je.isDirectory() || !je.getName().endsWith(".class"))
                    continue;
                
                String className = je.getName().replace("/", ".").replace(".class", "");
                className = className.replace('/', '.');
                
                try {
                    ret.add(cl.loadClass(className));
                } catch (Exception classLoadException) {
                    the.bytecode.club.bytecodeviewer.BytecodeViewer.handleException(classLoadException);
                }
            }
            
            jarFile.close();

            return ret;
        } catch (Exception e) {
            the.bytecode.club.bytecodeviewer.BytecodeViewer.handleException(e);
        }
        return null;
    }

    /**
     * Creates a new instance of the ClassNode loader.
     */
    public static void createNewClassNodeLoaderInstance() {
        loader.clear();
        loader = new ClassNodeLoader();
    }

    /**
     * Used to start a plugin from file.
     *
     * @param plugin the file of the plugin
     */
    public static void startPlugin(File plugin) {
        the.bytecode.club.bytecodeviewer.BytecodeViewer.startPlugin(plugin);
    }

    /**
     * Used to load classes/jars into BCV.
     *
     * @param files       an array of the files you want loaded.
     * @param recentFiles if it should save to the recent files menu.
     */
    public static void openFiles(File[] files, boolean recentFiles) {
        the.bytecode.club.bytecodeviewer.BytecodeViewer.openFiles(files, recentFiles);
    }

    /**
     * Returns the currently opened class node, if nothing is opened it'll return null.
     *
     * @return The opened class node or a null if nothing is opened
     */
    public static ClassNode getCurrentlyOpenedClassNode() {
        return the.bytecode.club.bytecodeviewer.BytecodeViewer.getCurrentlyOpenedClassNode();
    }

    /**
     * Used to load a ClassNode.
     *
     * @param name the full name of the ClassNode
     * @return the ClassNode
     */
    public static ClassNode getClassNode(String name) {
        return the.bytecode.club.bytecodeviewer.BytecodeViewer
                .blindlySearchForClassNode(name);
    }

    /**
     * Used to grab the loaded ClassNodes.
     *
     * @return the loaded classes
     */
    public static ArrayList<ClassNode> getLoadedClasses() {
        return the.bytecode.club.bytecodeviewer.BytecodeViewer
                .getLoadedClasses();
    }

    /**
     * Used to insert a Bytecode Hook using EZ-Injection.
     *
     * @param hook
     */
    public static void insertHook(BytecodeHook hook) {
        EZInjection.hookArray.add(hook);
    }

    /**
     * This will ask the user if they really want to reset the workspace, then
     * it'll reset the work space.
     *
     * @param ask if it should ask the user about resetting the workspace
     */
    public static void resetWorkSpace(boolean ask) {
        the.bytecode.club.bytecodeviewer.BytecodeViewer.resetWorkspace(ask);
    }

    /**
     * If true, it will display the busy icon, if false it will remove it if
     * it's displayed.
     *
     * @param busy if it should display the busy icon or not
     */
    public static void setBusy(boolean busy) {
        the.bytecode.club.bytecodeviewer.BytecodeViewer.updateBusyStatus(busy);
    }

    /**
     * Sends a small window popup with the defined message.
     *
     * @param message the message you want to display
     */
    public static void showMessage(String message) {
        the.bytecode.club.bytecodeviewer.BytecodeViewer.showMessage(message);
    }

    /**
     * Asks if the user would like to overwrite the file
     */
    public static boolean canOverwriteFile(File file) {
        return DialogueUtils.canOverwriteFile(file);
    }
    
    public static void hideFrame(JFrame frame, long milliseconds)
    {
        new Thread(()->{
            long started = System.currentTimeMillis();
            while(System.currentTimeMillis()-started <= milliseconds)
            {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) { }
            }
    
            frame.setVisible(false);
        }, "Timed Swing Hide").start();
    }

    /**
     * Returns the wrapped Krakatau Decompiler instance.
     *
     * @return The wrapped Krakatau Decompiler instance
     */
    public static InternalDecompiler getKrakatauDecompiler() {
        return Decompiler.KRAKATAU_DECOMPILER.getDecompiler();
    }

    /**
     * Returns the wrapped Procyon Decompiler instance.
     *
     * @return The wrapped Procyon Decompiler instance
     */
    public static InternalDecompiler getProcyonDecompiler() {
        return Decompiler.PROCYON_DECOMPILER.getDecompiler();
    }

    /**
     * Returns the wrapped CFR Decompiler instance.
     *
     * @return The wrapped CFR Decompiler instance
     */
    public static InternalDecompiler getCFRDecompiler() {
        return Decompiler.CFR_DECOMPILER.getDecompiler();
    }

    /**
     * Returns the wrapped FernFlower Decompiler instance.
     *
     * @return The wrapped FernFlower Decompiler instance
     */
    public static InternalDecompiler getFernFlowerDecompiler() {
        return Decompiler.FERNFLOWER_DECOMPILER.getDecompiler();
    }

    /**
     * Returns the wrapped Krakatau Disassembler instance.
     *
     * @return The wrapped Krakatau Disassembler instance
     */
    public static InternalDecompiler getKrakatauDisassembler() {
        return Decompiler.KRAKATAU_DISASSEMBLER.getDecompiler();
    }
    
    /**
     * Returns the wrapped JD-GUI Decompiler instance.
     *
     * @return The wrapped JD-GUI Decompiler instance
     */
    public static InternalDecompiler getDJGUIDecompiler() {
        return Decompiler.JD_DECOMPILER.getDecompiler();
    }
    
    /**
     * Returns the wrapped JADX Decompiler instance.
     *
     * @return The wrapped JADX Decompiler instance
     */
    public static InternalDecompiler getJADXDecompiler() {
        return Decompiler.JADX_DECOMPILER.getDecompiler();
    }

    /**
     * Returns the wrapped Java Compiler instance.
     *
     * @return The wrapped Java Compiler instance
     */
    public static InternalCompiler getJavaCompiler() {
        return Compiler.JAVA_COMPILER.getCompiler();
    }

    /**
     * Returns the wrapped Krakatau Assembler instance.
     *
     * @return The wrapped Krakatau Assembler instance
     */
    public static InternalCompiler getKrakatauCompiler() {
        return Compiler.KRAKATAU_ASSEMBLER.getCompiler();
    }

    /**
     * Returns the wrapped Smali Assembler instance.
     *
     * @return The wrapped Smali Assembler instance
     */
    public static InternalCompiler getSmaliCompiler() {
        return Compiler.SMALI_ASSEMBLER.getCompiler();
    }
}