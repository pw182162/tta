package com.itunes.parser;

import com.xmltagparser.Tag;

interface PropertyTagHandler
{
	public void handlePropertyChange(String propertyName);
	public void handlePropertyValue(Tag propertyValue);
}
