using ReactNative.Bridge;
using System;
using System.Collections.Generic;
using Windows.ApplicationModel.Core;
using Windows.UI.Core;

namespace Gw.Nls.RNGwNls
{
    /// <summary>
    /// A module that allows JS to share data.
    /// </summary>
    class RNGwNlsModule : NativeModuleBase
    {
        /// <summary>
        /// Instantiates the <see cref="RNGwNlsModule"/>.
        /// </summary>
        internal RNGwNlsModule()
        {

        }

        /// <summary>
        /// The name of the native module.
        /// </summary>
        public override string Name
        {
            get
            {
                return "RNGwNls";
            }
        }
    }
}
