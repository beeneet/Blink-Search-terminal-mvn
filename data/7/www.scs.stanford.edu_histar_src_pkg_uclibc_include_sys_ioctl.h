copyright c 1991 92 93 94 96 98 99 free software foundation inc this file is part of the gnu c library the gnu c library is free software you can redistribute it and or modify it under the terms of the gnu lesser general public license as published by the free software foundation either version 2.1 of the license or at your option any later version the gnu c library is distributed in the hope that it will be useful but without any warranty without even the implied warranty of merchantability or fitness for a particular purpose see the gnu lesser general public license for more details you should have received a copy of the gnu lesser general public license along with the gnu c library if not write to the free software foundation inc 59 temple place suite 330 boston ma 02111 1307 usa ifndef _sys_ioctl_h define _sys_ioctl_h 1 include features h __begin_decls get the list of ioctl requests and related constants include bits ioctls h define some types used by ioctl requests include bits ioctl types h on a unix system the system sys ioctl h probably defines some of the symbols we define in sys ttydefaults h usually with the same values the code to generate bits ioctls h has omitted these symbols to avoid the conflict but a unix program expects sys ioctl h to define them so we must include sys ttydefaults h here include sys ttydefaults h perform the i o control operation specified by request on fd one argument may follow its presence and type depend on request return value depends on request usually 1 indicates error extern int ioctl int __fd unsigned long int __request __throw __end_decls endif sys ioctl h
