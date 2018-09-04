// ASM: a very small and fast Java bytecode manipulation framework
// Copyright (c) 2000-2011 INRIA, France Telecom
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions
// are met:
// 1. Redistributions of source code must retain the above copyright
// notice, this list of conditions and the following disclaimer.
// 2. Redistributions in binary form must reproduce the above copyright
// notice, this list of conditions and the following disclaimer in the
// documentation and/or other materials provided with the distribution.
// 3. Neither the name of the copyright holders nor the names of its
// contributors may be used to endorse or promote products derived from
// this software without specific prior written permission.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
// AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
// IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
// ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
// LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
// CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
// SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
// INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
// CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
// ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
// THE POSSIBILITY OF SUCH DAMAGE.
package net.simpleframework.lib.org.objectweb.asm.xml;

import org.xml.sax.helpers.AttributesImpl;

import net.simpleframework.lib.org.objectweb.asm.ModuleVisitor;
import net.simpleframework.lib.org.objectweb.asm.Opcodes;

/**
 * Generate SAX event for a module description.
 *
 * @deprecated This class is no longer maintained, will not support new Java
 *             features, and will
 *             eventually be deleted. Use the asm or asm.tree API instead.
 * @author Remi Forax
 */
@Deprecated
public final class SAXModuleAdapter extends ModuleVisitor {

	private final SAXAdapter sa;

	public SAXModuleAdapter(final SAXAdapter sa) {
		super(Opcodes.ASM6);
		this.sa = sa;
	}

	@Override
	public void visitMainClass(final String mainClass) {
		final AttributesImpl att = new AttributesImpl();
		att.addAttribute("", "name", "name", "", mainClass);
		sa.addElement("main-class", att);
	}

	@Override
	public void visitPackage(final String packaze) {
		final AttributesImpl att = new AttributesImpl();
		att.addAttribute("", "name", "name", "", packaze);
		sa.addElement("packages", att);
	}

	@Override
	public void visitRequire(final String module, final int access, final String version) {
		final AttributesImpl att = new AttributesImpl();
		final StringBuilder sb = new StringBuilder();
		SAXClassAdapter.appendAccess(access | SAXClassAdapter.ACCESS_MODULE, sb);
		att.addAttribute("", "module", "module", "", module);
		att.addAttribute("", "access", "access", "", sb.toString());
		if (version != null) {
			att.addAttribute("", "version", "version", "", version);
		}
		sa.addElement("requires", att);
	}

	@Override
	public void visitExport(final String packaze, final int access, final String... modules) {
		final AttributesImpl att = new AttributesImpl();
		final StringBuilder sb = new StringBuilder();
		SAXClassAdapter.appendAccess(access | SAXClassAdapter.ACCESS_MODULE, sb);
		att.addAttribute("", "name", "name", "", packaze);
		att.addAttribute("", "access", "access", "", sb.toString());
		sa.addStart("exports", att);
		if (modules != null && modules.length > 0) {
			for (final String to : modules) {
				final AttributesImpl atts = new AttributesImpl();
				atts.addAttribute("", "module", "module", "", to);
				sa.addElement("to", atts);
			}
		}
		sa.addEnd("exports");
	}

	@Override
	public void visitOpen(final String packaze, final int access, final String... modules) {
		final AttributesImpl att = new AttributesImpl();
		final StringBuilder sb = new StringBuilder();
		SAXClassAdapter.appendAccess(access | SAXClassAdapter.ACCESS_MODULE, sb);
		att.addAttribute("", "name", "name", "", packaze);
		att.addAttribute("", "access", "access", "", sb.toString());
		sa.addStart("opens", att);
		if (modules != null && modules.length > 0) {
			for (final String to : modules) {
				final AttributesImpl atts = new AttributesImpl();
				atts.addAttribute("", "module", "module", "", to);
				sa.addElement("to", atts);
			}
		}
		sa.addEnd("opens");
	}

	@Override
	public void visitUse(final String service) {
		final AttributesImpl att = new AttributesImpl();
		att.addAttribute("", "service", "service", "", service);
		sa.addElement("uses", att);
	}

	@Override
	public void visitProvide(final String service, final String... providers) {
		final AttributesImpl att = new AttributesImpl();
		att.addAttribute("", "service", "service", "", service);
		sa.addStart("provides", att);
		for (final String provider : providers) {
			final AttributesImpl atts = new AttributesImpl();
			atts.addAttribute("", "provider", "provider", "", provider);
			sa.addElement("with", atts);
		}
		sa.addEnd("provides");
	}

	@Override
	public void visitEnd() {
		sa.addEnd("module");
	}
}
