package com.stovokor.domain;

import java.util.Random;

import com.jme3.math.ColorRGBA;

public abstract class ShieldColor {

	public static ShieldColor none;
	public static ShieldColor red;
	public static ShieldColor green;
	public static ShieldColor blue;
	public static ShieldColor yellow;
	public static ShieldColor magenta;
	public static ShieldColor cyan;
	public static ShieldColor white;
	
	private static ShieldColor[] colors;
	
	static {
		none = new ShieldColor() {
			@Override
			public ColorRGBA getColor() {
				return ColorRGBA.Black;
			}};
		red = new ShieldColor() {
			@Override
			public ColorRGBA getColor() {
				return ColorRGBA.Red;
			}};
		green = new ShieldColor() {
			@Override
			public ColorRGBA getColor() {
				return ColorRGBA.Green;
			}};
		blue = new ShieldColor() {
			@Override
			public ColorRGBA getColor() {
				return ColorRGBA.Blue;
			}};
		yellow = new ShieldColor() {
			@Override
			public ColorRGBA getColor() {
				return ColorRGBA.Yellow;
			}};
		magenta = new ShieldColor() {
			@Override
			public ColorRGBA getColor() {
				return ColorRGBA.Magenta;
			}};
		cyan = new ShieldColor() {
			@Override
			public ColorRGBA getColor() {
				return ColorRGBA.Cyan;
			}};
		white = new ShieldColor() {
			@Override
			public ColorRGBA getColor() {
				return ColorRGBA.White;
			}};
		
		colors = new ShieldColor[8];
		colors[0] = none;
		colors[1] = red;
		colors[2] = green;
		colors[4] = blue;
		colors[3] = yellow;
		colors[5] = magenta;
		colors[6] = cyan;
		colors[7] = white;
	}

	
	public static ShieldColor fromMask(boolean red, boolean green, boolean blue) {
		int n = (blue ? 1 << 2 : 0) +
				(green ? 1 << 1 : 0) +
				(red ? 1 : 0);
		return colors[n];
	}
	
	public abstract ColorRGBA getColor();

	public static ShieldColor random() {
		int r = new Random().nextInt(7) + 1;
		return colors[r];
	}
}


